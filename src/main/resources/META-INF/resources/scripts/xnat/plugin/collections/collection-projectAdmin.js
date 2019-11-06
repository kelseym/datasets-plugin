/*
 * web: collection-projectAdmin.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Site-wide Admin UI functions for Clara UI
 */

console.log('collection-projectAdmin.js');

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

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    var undefined,
        projectId = XNAT.data.context.project,
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
        csrfUrl = XNAT.url.csrfUrl;

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function unCamelCase(string){
        string = string.replace(/([A-Z])/g, " $1"); // insert a space before all capital letters
        return string.charAt(0).toUpperCase() + string.slice(1); // capitalize the first letter to title case the string
    }

    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: ' + title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function () {
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    /* =========================== *
     * DISPLAY LIST OF COLLECTIONS  *
     * =========================== */

    var projCollections;

    XNAT.plugin.collection.projCollections = projCollections =
        getObject(XNAT.plugin.collection.projCollections || {});

    projCollections.savedCollections = [];
    projCollections.availableExpts = [];

    var getCollectionsUrl = function(){
        return rootUrl("/xapi/collection/getAllForProject/"+projectId);
    };

    // create data table
    projCollections.table = function(container, callback) {

        // initialize the table - we'll add to it below
        var pjcTable = XNAT.table({
            className: 'collection-list xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        // add table header row
        pjcTable.tr()
            .th({addClass: 'left', html: '<b>Name</b>'})
            .th('<b>Description</b>')
            .th('<b>Contents</b>')
            .th('<b>Created</b>')
            .th('<b>Actions</b>');

        function listObj(obj){
            var keys = Object.keys(obj), text = "";
            keys.forEach(function(key){
                text += key + ": [" + obj[key].length + " items]<br>";
            });
            return text;
        }
        function isoDate(timestamp){
            var date = new Date(timestamp);
            return date.toISOString().slice(0,10);
        }
        function viewButton(id){
            return spawn('button.btn.btn-sm.view-collection',{
                onclick: function(e){
                    e.preventDefault();
                    XNAT.plugin.collection.projCollections.show(id)
                }
            }, 'View / Edit')
        }
        function editButton(id){

        }

        // get project models
        XNAT.xhr.getJSON({
            url: getCollectionsUrl(),
            fail: function(e){ errorHandler(e,"Could not retrieve collections for "+projectId)},
            success: function(data){
                // returns an array, not an object
                projCollections.savedCollections = data;

                if(projCollections.savedCollections.length){
                    projCollections.savedCollections.forEach(function(collection){
                        var buckets = identifyBuckets(collection);

                        pjcTable.tr({ id: collection.id, data: {"model" : collection.label }})
                            .td(collection.name)
                            .td(collection.description)
                            .td(listObj(buckets))
                            .td(isoDate(collection.timestamp))
                            .td([[ 'div.center', [ viewButton(collection.id) ]]])
                    });
                }
                else {
                    pjcTable.tr()
                        .td({ colspan: 5, html: 'No data collections have been set up in this project.'});
                }
                if (container){
                    $$(container).append(pjcTable.table);
                }

                if (isFunction(callback)) {
                    callback(pjcTable.table);
                }
            }
        })
    };

    function identifyBuckets(collection){
        var buckets = {}, params = Object.keys(collection);
        params.forEach(function(param){ if (isArray(collection[param])) buckets[param] = collection[param] });
        return buckets;
    }

    projCollections.sanitizeCollection = function(collection){
        var toRemove = ["id","timestamp","enabled","disabled","created"];
        toRemove.forEach(function(key){ delete collection[key] });
        return collection;
    };

    projCollections.viewSource = function(source,title){
        title = title || 'View Source';
        _source = spawn ('textarea', JSON.stringify(source, null, 4));

        _editor = XNAT.app.codeEditor.init(_source, {
            language: 'json'
        });

        _editor.openEditor({
            title: title,
            classes: 'plugin-json',
            buttons: {
                ok: {
                    label: 'OK',
                    isDefault: true,
                    close: true
                }
            },
            height: 680,
            afterShow: function(dialog, obj){
                obj.aceEditor.setReadOnly(true);
            }
        });
    };

    projCollections.editSource = function(source,title,id){
        title = title || 'View Source';
        var sanitizedSource = projCollections.sanitizeCollection(source);
        _source = spawn ('textarea', JSON.stringify(sanitizedSource, null, 4));

        _editor = XNAT.app.codeEditor.init(_source, {
            language: 'json'
        });

        _editor.openEditor({
            title: title,
            classes: 'plugin-json',
            buttons: {
                ok: {
                    label: 'OK',
                    isDefault: true,
                    close: true
                }
            },
            height: 680,
            beforeShow: function(dialog,obj){
                dialog.$modal.find('.body .inner').prepend(spawn('!',[
                    XNAT.ui.panel.input.hidden({
                        name: 'id',
                        value: id
                    }),
                    XNAT.ui.panel.element({
                        label: 'ID',
                        value: id
                    }).spawned
                ]))
            },
            afterShow: function(dialog, obj){
                obj.aceEditor.setReadOnly(false);
            }
        });
    };

    projCollections.show = function(id){
        var collection = projCollections.savedCollections.filter(function(a){ return a.id === id })[0];

        XNAT.dialog.open({
            title: 'Data Collection: '+collection.name,
            width: 600,
            content: spawn('div.collection-dialog-viewer'),
            beforeShow: function(obj){
                var viewer = obj.$modal.find('.collection-dialog-viewer');
                viewer.append(spawn('!',[
                    XNAT.ui.panel.element({
                        label: 'Collection Name',
                        value: collection.name
                    }),
                    XNAT.ui.panel.element({
                        label: 'Description',
                        value: collection.description
                    })
                ]));
                var buckets = identifyBuckets(collection);
                var bucketTypes = Object.keys(buckets);
                bucketTypes.forEach(function(type){
                    var bucketContents = [];
                    buckets[type].forEach(function(element){
                        var label = XNAT.plugin.collection.availableExpts.filter(function(item){ return item.ID === element })[0].label;
                        bucketContents.push(label + " ("+element+")");
                    });
                    viewer.append(spawn('!',[
                        XNAT.ui.panel.element({
                            label: unCamelCase(type),
                            value: '('+buckets[type].length+' items)'
                        }).spawned,
                        spawn('div.inset-viewer',{
                            html: bucketContents.join(', ')
                        })
                    ]))
                })
            },
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true
                },
                {
                    label: 'Edit Original',
                    close: false,
                    action: function(){
                        // projCollections.viewSource(collection, 'RAW Collection JSON for '+collection.name);
                        projCollections.editSource(collection, 'RAW Collection JSON for '+collection.name, collection.id);
                    }
                }
            ]
        })
    };

    function showCollectionCount(){
        if (projCollections.savedCollections.length) {
            $(document).find('.collection-count').append(" ("+projCollections.savedCollections.length+")");
        }
    }

    projCollections.init = function(){
        var $collectionList = $('div#proj-data-collection-list-container');
        $collectionList.empty();
        projCollections.table($collectionList,showCollectionCount);

        var $footer = $collectionList.parents('.panel').find('.panel-footer');
        var newCollection = spawn('button.new-collection.btn.btn-sm.submit', {
            html: 'New Data Collection',
            onclick: function(){
                XNAT.plugin.collections.collectionCreator.open();
            }
        });

        // add the 'add new' button to the panel footer
        $footer.append(spawn('div.pull-right', [
            newCollection
        ]));
        $footer.append(spawn('div.clear.clearFix'));

    };

    try {
        projCollections.init();
    } catch(e) {
        errorHandler(e);
    }

}));