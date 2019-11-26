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
    var supportedDataTypes = {};
    XNAT.app.dataTypeAccess.getElements['browseable'].ready(function(elements){
        supportedDataTypes = elements.elementMap;
    });


    XNAT.plugin.collection.getProjectExperiments = function(project){
        XNAT.xhr.getJSON({
            url: rootUrl("/data/projects/"+project+"/experiments?format=json"),
            fail: function(e){ errorHandler(e, "Could not retrieve experiments for project "+project )},
            success: function(data){
                XNAT.plugin.collection.availableExpts = data.ResultSet.Result;
            }
        })
    };
    function sortByXsiType(experiments){
        var xsiTypes = [], sortedExperiments = {};
        experiments.forEach(function(expt){
            if (xsiTypes.indexOf(expt.xsiType) < 0) xsiTypes.push(expt.xsiType)
        });
        xsiTypes.forEach(function(type){
            sortedExperiments[type] = experiments.filter(function(expt){ return expt.xsiType === type})
        });
        return sortedExperiments;
    }

    // populate available experiments and sort them by xsitype
    $(document).ready(function(){
        XNAT.plugin.collection.getProjectExperiments(projectId);
    });

    /* ============================================== *
     * CREATE A COLLECTION FROM AVAILABLE EXPERIMENTS *
     * ============================================== */

    var collectionCreator;
    XNAT.plugin.collection.collectionCreator = collectionCreator =
        getObject(XNAT.plugin.collection.collectionCreator || {});

    function buildSelectableTable(container, experiments, xsitype){
        var $container = $(container);
        $container.append(spawn('h3',{ style: { margin: '2em 0 1em' }}, supportedDataTypes[xsitype].plural));

        var tableHeader = spawn('div.data-table-wrapper.no-body',{ style: { 'border':'none' }}, [
            spawn('table.xnat-table.fixed-header.clean', { style: { 'border-bottom':'none' }}, [
                spawn('thead',[
                    spawn('tr',[
                        spawn('th.toggle-all',{ style: { width: '45px' }},[
                            spawn('input.selectable-select-all|type=checkbox',{ title: 'Toggle All '+supportedDataTypes[xsitype].plural })
                        ]),
                        spawn('th.left',{ style: { width: '250px' }},'Label'),
                        spawn('th.left',{ style: { width: '263px' }},'XNAT Accession ID')
                    ])
                ])
            ])
        ]);

        var tableBodyRows = [];
        // loop over an array of data, populate the table body rows
        // max table width in a 700-px dialog is 658px
        experiments.forEach(function(experiment){
            tableBodyRows.push(
                spawn('tr.selectable-tr',{ id: experiment['ID'] },[
                    spawn('td.table-action-controls.table-selector.center',{ style: { width: '45px' }}, [
                        spawn('input.selectable-select-one.target|type=checkbox', { value: experiment['ID'] })
                    ]),
                    spawn('td',[
                        spawn('span',{ style: { width: '226px', 'word-wrap':'break-word', 'display': 'inline-block' }},experiment['label'])
                    ]),
                    spawn('td',[
                        spawn('span',{ style: { width: '239px', 'word-wrap':'break-word', 'display': 'inline-block' }},experiment['ID'])
                    ])
                ])
            );
        });

        var tableBody = spawn('div.data-table-wrapper.no-header',{
            style: {
                'border-color': '#aaa',
                'max-height': '300px',
                'overflow-y': 'auto'
            }
        },[
            spawn('table.xnat-table.clean.selectable',{ style: { 'border':'none' }}, [
                spawn('tbody', tableBodyRows )
            ])
        ]);

        container.append(
            spawn('div.data-table-container.experiment-selector',[ tableHeader, tableBody ])
        );
    }

    collectionCreator.openDialog = function(collection){
        collection = collection || { "list": [], "labels:": [] };

        collection.labels.forEach(function(val,i){
            collection.labels[i] = '<li>'+val+'</li>';
        });

        function listSessions(labels){
            return spawn('ul',{style: { 'list-style-type':'none'}},labels.join(''));
        }

        function getSelectedExperiments(form){
            var selectedExperiments = [];
            $(form).find('.experiment-selector').each(function(){
                $(this).find('tbody').find('input[type=checkbox]:checked').each(function(){
                    selectedExperiments.push($(this).val());
                });
            });
            return selectedExperiments;
        }

        XNAT.dialog.open({
            title: 'Create Data Collection',
            width: 600,
            content: spawn('form.collectionModalContent',[ spawn('div.panel') ]),
            beforeShow: function(obj){
                var inputArea = obj.$modal.find('.collectionModalContent').find('.panel');
                inputArea.append(spawn('!',[
                    spawn('div.message',{ style: { 'margin-bottom': '1em' }},'Select the experiments you want to include in this data collection. XNAT will randomly sort your selected experiments into a 70/20/10 Train/Validation/Test format.'),
                    XNAT.ui.panel.input({ name: "name", label: "Collection Title", addClass: "required" }),
                    XNAT.ui.panel.input({ name: "description", label: "Description" }),
                ]));

                var sortedExperiments = sortByXsiType(XNAT.plugin.collection.availableExpts);
                var xsitypes = Object.keys(sortedExperiments);
                xsitypes.forEach(function(type){
                    buildSelectableTable(inputArea,sortedExperiments[type],type);
                })
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
                        var experiments = getSelectedExperiments(formData);

                        if (!experiments.length) {
                            XNAT.dialog.message('Error: No experiments selected.');
                            return false;
                        }

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
                                XNAT.dialog.message('Created Collection Set "'+data.name+'" with '+data.trainingExperiments.length+' Training, '+data.validationExperiments.length+' Validation, and '+data.testExperiments.length+' Test experiments.');
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