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

    var projDatasets, savedDatasets;

    XNAT.plugin.collection.projDatasets = projDatasets =
        getObject(XNAT.plugin.collection.projDatasets || {});

    projDatasets.savedDatasets = savedDatasets = [];
    projDatasets.availableExpts = [];

    var getDatasetsUrl = function() {
        return rootUrl("/xapi/sets/collections/projects/" + projectId);
    };

    /* --- display and manage datasets --- */

    projDatasets.viewDataset = function(dataset) {

        var vdTable = XNAT.table({
            className: 'xnat-table compact',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        var allTables = [spawn('h3', 'Contents of Saved Dataset'), vdTable.table];

        for (var key in dataset) {
            var val = dataset[key], formattedVal = '', putInTable = true;

            if (Array.isArray(val) && val.length > 0) {
                // Display a table
                var columns = [];
                val.forEach(function (item) {
                    if (typeof item === 'object') {
                        Object.keys(item).forEach(function(itemKey){
                            if(columns.indexOf(itemKey)===-1){
                                columns.push(itemKey);
                            }
                        });
                    }
                });

                formattedVal="<table class='xnat-table'>";

                val.forEach(function (item) {
                    formattedVal+="<tr>";
                    if (typeof item === 'object') {
                        columns.forEach(function (itemKey) {
                            formattedVal += "<td nowrap>";
                            var temp = item[itemKey];
                            if (typeof temp === 'object') temp = JSON.stringify(temp);
                            formattedVal += temp;
                            formattedVal += "</td>";
                        });
                    } else {
                        formattedVal += "<td nowrap>";
                        formattedVal += item;
                        formattedVal += "</td>";
                    }
                    formattedVal+="</tr>";
                });
                formattedVal+="</table>";
                putInTable = false;
            } else if (typeof val === 'object') {
                formattedVal = spawn('code', JSON.stringify(val));
            } else if (!val) {
                formattedVal = spawn('code', 'false');
            } else {
                formattedVal = spawn('code', val);
            }

            if (putInTable) {
                vdTable.tr()
                    .td('<b>' + key + '</b>')
                    .td([spawn('div', {style: {'word-break': 'break-all', 'max-width': '600px', 'overflow':'auto'}}, formattedVal)]);
            } else {
                allTables.push(
                    spawn('div', {style: {'word-break': 'break-all', 'overflow':'auto', 'margin-bottom': '10px', 'max-width': 'max-content'}},
                        [spawn('div.data-table-actionsrow', {}, spawn('strong', {class: "textlink-sm data-table-action"}, key)), formattedVal])
                );
            }
        }

        // display history
        XNAT.ui.dialog.open({
            title: dataset['label'],
            width: 800,
            scroll: true,
            content: spawn('div', allTables),
            buttons: {
                ok: {
                    label: 'OK',
                    isDefault: true,
                    close: true
                }
            },
            header: true,
            maxBtn: true
        });
    };

    projDatasets.displaySavedDatasets = function(datasets){
        function viewButton(id){
            return spawn('button.btn.btn-sm.view-dataset',{ title: 'View Dataset', data: {id:id}},[ spawn('i.fa.fa-eye')])
        }
        function deleteButton(dataset){
            return spawn('button.btn.btn-sm.delete-dataset', { title: 'Delete Dataset', data: {id:dataset.id, label:dataset.label}},[ spawn('i.fa.fa-trash-o')])
        }
        function datasetLink(dataset){
            return spawn('a.view-dataset',{href: 'javascript:void', data: {id: dataset.id}},dataset.label);
        }
        function resolveDate(label){
            var timestamp = label.split('-')[label.split('-').length-1];
            var d = new Date(timestamp*1); // simple hack to convert string to integer
            return spawn('span',d.toUTCString());
        }

        var sdTable = XNAT.table({addClass: 'xnat-table', style: { width: '100%' }});
        sdTable.tr()
            .th('Dataset Label')
            .th('Number of Files')
            .th('Date Created')
            .th('Actions');

        datasets.forEach(function(dataset){
            sdTable.tr()
                .td({ addClass: 'first-cell'},[[ '!',datasetLink(dataset) ]])
                .td('')
                .td([[ '!',resolveDate(dataset.label)]])
                .td([[ 'div.center', [
                    viewButton(dataset.id),
                    spacer(6),
                    deleteButton(dataset)]
                ]])
        });

        var container = $('#proj-saved-datasets-list-container');
        container.empty().append(sdTable.table);
    };

    projDatasets.resetSavedDatasets = function(){
        var container = $('#proj-saved-datasets-list-container');
        container.empty().append(spawn('div.message','No datasets have been defined in this project.'))
    };

    projDatasets.getSavedDatasets = function(){
        XNAT.xhr.getJSON({
            url: rootUrl('/xapi/sets/collections/projects/'+projectId),
            fail: function(e){ errorHandler(e,'Error trying to retrieve saved dataset for '+projectId+'.')},
            success: function(data){
                if (data.length) {
                    XNAT.plugin.collection.projDatasets.savedDatasets = data;
                    projDatasets.displaySavedDatasets(data);
                } else {
                    projDatasets.resetSavedDatasets();
                }
            }
        })
    };

    $(document).on('click','.view-dataset',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        XNAT.xhr.getJSON({
            url: rootUrl('/xapi/sets/collections/'+id),
            fail: function(e){ errorHandler(e,'Error trying to get the dataset '+id)},
            success: function(data){
                XNAT.plugin.collection.projDatasets.viewDataset(data);
            }
        })
    });
    $(document).on('click','.delete-dataset',function(e){
        e.preventDefault();
        var id = $(this).data('id'),
            label = $(this).data('label');
        XNAT.xhr.ajax({
            method: 'DELETE',
            url: csrfUrl('/xapi/sets/collections/'+id),
            fail: function(e){ errorHandler(e,'Error trying to delete the dataset '+id)},
            success: function(){
                XNAT.ui.banner.top(2000,'Successfully deleted the dataset '+label,'success');
                XNAT.plugin.collection.projDatasets.init();
            }
        })
    });

    function showCollectionCount(){
        if (projDatasets.savedDatasets.length) {
            $(document).find('.collection-count').append(" ("+projDatasets.savedDatasets.length+")");
        }
    }

    projDatasets.init = projDatasets.refresh = function(refresh){
        refresh = refresh || false;

        projDatasets.resetSavedDatasets();
        projDatasets.getSavedDatasets();
    };

    try {
        projDatasets.init();
    } catch(e) {
        errorHandler(e);
    }

}));
