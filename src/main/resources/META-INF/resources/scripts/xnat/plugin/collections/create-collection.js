console.log('create-collection.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.collection = getObject(XNAT.plugin.collection || {});

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {
    var undefined,
        projectId = XNAT.data.context.project,
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
        csrfUrl = XNAT.url.csrfUrl;

    function errorHandler(e, title, closeAll){
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }
    /* ========================== *
     * FIND AVAILABLE EXPERIMENTS *
     * ========================== */

    XNAT.plugin.collection.availableExpts = [];

    var getProjectDataUrl = function(){
        return rootUrl("/data/projects/"+projectId+"/experiments?format=json")
    };

    function getProjectExperiments(){
        XNAT.xhr.getJSON({
            url: getProjectDataUrl(),
            fail: function(e){ errorHandler(e, "Could not retrieve experiments for project "+projectId )},
            success: function(data){
                XNAT.plugin.collection.availableExpts = data.ResultSet.Result;
            }
        })
    }
    $(document).ready(function(){
        // after primary page functions are available, we can populate this list for future use
        getProjectExperiments();
    });

    /* ============================================== *
     * CREATE A COLLECTION FROM AVAILABLE EXPERIMENTS *
     * ============================================== */

    var collectionCreator;
    XNAT.plugin.collection.collectionCreator = collectionCreator =
        getObject(XNAT.plugin.collection.collectionCreator || {});

    collectionCreator.openDialog = function(collection){
        collection = collection || { "list": [], "labels:": [] };

        collection.labels.forEach(function(val,i){
            collection.labels[i] = '<li>'+val+'</li>';
        });

        function listSessions(labels){
            return spawn('ul',{style: { 'list-style-type':'none'}},labels.join(''));
        }

        XNAT.dialog.open({
            title: 'Create Data Collection',
            width: 600,
            content: spawn('form.collectionModalContent',[ spawn('div.panel') ]),
            beforeShow: function(obj){
                var inputArea = obj.$modal.find('.collectionModalContent').find('.panel');
                inputArea.append(spawn('!',[
                    XNAT.ui.panel.input({ name: "name", label: "Collection Title", addClass: "required" }),
                    XNAT.ui.panel.input({ name: "description", label: "Description" }),
                    XNAT.ui.panel.input.hidden({ name: "experiments", value: JSON.stringify(collection.list), addClass: "required" })
                ]));
                inputArea.append(spawn('!',[
                    XNAT.ui.panel.element({ label: "Sessions in Collection", html: listSessions(collection.labels) }).spawned
                ]));
            },
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: false,
                    action: function(obj){
                        var formData = obj.$modal.find('form');
                        var name = formData.find('input[name=name]').val();
                        var description = formData.find('input[name=description]').val();
                        var experiments = JSON.parse(formData.find('input[name=experiments]').val());
                        var collectionModel = {
                            "name": name,
                            "description": description,
                            "experiments": experiments,
                            "projectId": projectId
                        };
                        XNAT.xhr.ajax({
                            url: XNAT.url.csrfUrl('/xapi/collection/createFromSet'),
                            method: 'POST',
                            contentType: 'application/json',
                            data: JSON.stringify(collectionModel),
                            processData: false,
                            fail: function(e){ errorHandler(e,'Could not Create Collection Set') },
                            success: function(data){
                                XNAT.ui.dialog.closeAll();
                                console.log(data);
                                XNAT.ui.banner.top('Created Collection Set','success','3000');
                            }
                        })
                    }
                }
            ]
        })
    };

    collectionCreator.open = function(xsitype){
        var expts = XNAT.plugin.collection.availableExpts;
        if (xsitype) {
            expts = expts.filter(function(expt){ return expt.xsiType === xsitype });
        }
        else {
            xsitype = '(Any)';
        }

        if (!expts.length) {
            errorHandler({
                status: 'Unexpected',
                statusText: 'Empty Set',
                responseText: 'No experiments available with xsiType: ' + xsitype
            }, 'Could Not Create Collection');
            return false;
        }

        var collectionData = { list: [], labels: [] };
        expts.forEach(function(result){
            collectionData.list.push(result.ID);
            collectionData.labels.push(result.label);
        });
        collectionCreator.openDialog(collectionData);

    }

}));