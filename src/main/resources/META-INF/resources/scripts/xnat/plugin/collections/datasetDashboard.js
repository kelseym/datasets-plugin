console.log('datasetDashboard.js');

var XNAT = getObject(XNAT || {});

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

    var undef;
    var plugin, collection, sets, definitions, datasets;

    XNAT.plugin = plugin =
        getObject(XNAT.plugin || {});

    XNAT.plugin.collection = collection =
        getObject(XNAT.plugin.collection || {});

    XNAT.plugin.collection.sets = sets =
        getObject(XNAT.plugin.collection.sets || {});

    XNAT.plugin.collection.sets.definitions = definitions =
        getObject(XNAT.plugin.collection.sets.definitions || []);

    XNAT.plugin.collection.sets.datasets = datasets =
        getObject(XNAT.plugin.collection.sets.datasets || []);

    var projectId = XNAT.data.context.project;
    var rootUrl = XNAT.url.rootUrl;
    var restUrl = XNAT.url.restUrl;
    var csrfUrl = XNAT.url.csrfUrl;

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll){
        console.warn(e);
        title        = (title) ? 'Error Found: ' + title : 'Error';
        closeAll     = (closeAll === undef) ? true : closeAll;
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
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    /* --- handle dataset definitions --- */

    function getSingleDefinition(id){
        return definitions.filter(function(d){ return d.id === id})[0];
    }
    sets.deleteDefinition = function(id){
        XNAT.xhr.ajax({
            method: 'DELETE',
            url: csrfUrl('/xapi/sets/definitions/projects/'+projectId+'/'+id),
            fail: function(e){ errorHandler(e, 'Error attempting to delete dataset with id '+id)},
            success: function(){
                XNAT.ui.banner.top(2000, 'Definition deleted.', 'success');
                sets.initDashboard();
            }
        })
    };

    $(document).on('click','.validate-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        sets.validateDefinition(id);
    });
    $(document).on('click','.edit-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        var dfn = getSingleDefinition(id);
        XNAT.plugin.collection.sets.openDefinitionEditor(dfn);
    });
    $(document).on('click','.delete-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        sets.deleteDefinition(id);
    });


    function displayDatasetDefinitions(){
        function validateButton(id){
            return spawn('button.btn.btn-sm.validate-definition',{ title: 'Validate Data with this Definition', data: {id:id} },[ spawn('i.fa.fa-cogs')])
        }
        function editButton(id){
            return spawn('button.btn.btn-sm.edit-definition',{ title: 'Edit Definition', data: {id:id}},[ spawn('i.fa.fa-pencil')])
        }
        function deleteButton(id){
            return spawn('button.btn.btn-sm.delete-definition', { title: 'Delete Definition', data: {id:id}},[ spawn('i.fa.fa-trash-o')])
        }

        var ddTable = XNAT.table({addClass: 'xnat-table', style: { width: '100%' }});
        ddTable.tr()
            .th('Dataset Criteria')
            .th('Type')
            .th('Description')
            // .th('Last Modified')
            .th('Actions');

        definitions.forEach(function(definition){
            var criteria = definition.criteria[0];
            ddTable.tr()
                .td({ style: {'font-weight': 'bold'}},definition.label)
                .td(criteria.resolver)
                .td({ addClass: 'dataset-definition-description' },definition.description)
                // .td('')
                .td([[ 'div.center', [
                    validateButton(definition.id),
                    spacer(6),
                    editButton(definition.id),
                    spacer(6),
                    deleteButton(definition.id)]
                ]])
        });

        var container = $('#proj-dataset-criteria-list-container');
        container.empty().append(ddTable.table);
    }

    function resetDatasetDefinitions(){
        var container = $('#proj-dataset-criteria-list-container');
        container.empty().append(spawn('div.message','No dataset criteria have been defined in this project. Currently supported dataset types are: "TaggedResourceMap (ClaraTrain)".'))
    }

    function getDatasetDefinitions(){
        XNAT.xhr.get({
            url: rootUrl('/xapi/sets/definitions/projects/'+projectId),
            fail: function(e){ errorHandler(e,'Error trying to retrieve dataset definitions for '+projectId+'.')},
            success: function(data){
                if (data.length) {
                    definitions = data;
                    displayDatasetDefinitions();
                } else {
                    resetDatasetDefinitions();
                }
            }
        })
    }

    /* --- handle dataset validations --- */

    // Temporary validation URL
    function getValidationUrl(id){
        var swaggerhub = 'https://virtserver.swaggerhub.com/hortonw/detailed_dataset_report/1.0.0';
        return swaggerhub + '/sets/definitions/detailed_report/'+projectId+'/'+id;
    }
    function evaluateResult(experiment,list){
        var valid = true;
        experiment.results.forEach(function(result){ if (!result.result) { valid = false }});
        if (valid) { list.push(experiment.id) }
        return list;
    }
    
    function displayValidationResults(items){
        var allExperiments = items.length,
            validExperiments = [],
            vrTable = XNAT.table({addClass: 'xnat-table', style: { width: '100%' }});

        var headerRow = vrTable.tr()
            .th({ addClass: 'align-left' },'Experiments');

        // validation columns can be parsed from the items in the first row of results.
        var firstResult = items[0].results;
        firstResult.forEach(function(criteria){
            var columnHeader = criteria.file +':<br>'+criteria.check;
            columnHeader += (criteria.matcher) ? '<br>=['+criteria.matcher +']': '';
            headerRow.th({ addClass: 'align-left' },columnHeader);
        });

        function experimentLink(experiment){
            return spawn('a',{ href: rootUrl('/data/experiments/'+experiment.id), style: { 'font-weight':'bold' }}, experiment.label)
        }
        function displayIcon(validation){
            return (validation.result) ?
                spawn('i.fa.fa-check-circle.success') :
                spawn('i.fa.fa-close.failed')
        }
        items.forEach(function(item){
            var experimentRow = vrTable.tr()
                .td({ addClass: 'nowrap' },[['!',experimentLink(item)]]);
            evaluateResult(item,validExperiments);
            item.results.forEach(function(validation){
                experimentRow
                    .td([['div.center',[ displayIcon(validation) ]]])
            })
        });

        var container = $('#proj-dataset-validation-table-container');
        container.empty();
        container.append(spawn('!',[
            spawn('p.table-summary', validExperiments.length+' of '+allExperiments+' sessions complete'),
            spawn('div.panel-table-container',[
                vrTable.table
            ])
        ]));
    }

    $(document).on('click','.save-dataset',function(e){
        e.preventDefault();
        if($(this).hasClass('disabled')) return false;
        var id = $(this).data('id');
        sets.saveDataset(id);
    });

    function enablePanel(id){
        $('#save-dataset-button').data('id',id).removeClass('disabled');
        $('#project-dataset-validation-panel').removeClass('disabled');
    }
    function disablePanel(){
        $('#save-dataset-button').data('id','').addClass('disabled');
        $('#project-dataset-validation-panel').addClass('disabled');
    }

    sets.validateDefinition = function(id){
        var dfn = getSingleDefinition(id);
        xmodal.loading.open('Validating Project Data');
        XNAT.xhr.get({
            url: getValidationUrl(id),
            fail: function(e){
                xmodal.loading.close();
                errorHandler(e,'Error trying to query the dataset validation URL for dataset definition '+id);
                disablePanel();
            },
            success: function(data){
                xmodal.loading.close();
                if (data.length) {
                    displayValidationResults(data,dfn);
                    enablePanel(id);
                } else {
                    XNAT.dialog.message('Error','Could not validate your project data against this data definition. No results were returned.');
                }
            }
        });
    };

    sets.saveDataset = function(id){
        XNAT.xhr.ajax({
            method: 'POST',
            url: csrfUrl('/xapi/sets/definitions/'+id),
            fail: function(e){ errorHandler(e,'Error trying to save dataset from definition '+id); },
            success: function(data){
                XNAT.ui.banner.top(2000,'New dataset '+data.label+' created with '+data.fileCount+' files.','success');
            }
        })
    };

    /* --- display and manage datasets --- */

    sets.viewDataset = function(dataset) {

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

    function displaySavedDatasets(){
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
            var timestamp = label.split('-')[1];
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
    }

    function getSavedDatasets(){
        XNAT.xhr.get({
            url: rootUrl('/xapi/sets/collections/projects/'+projectId),
            fail: function(e){ errorHandler(e,'Error trying to retrieve saved dataset for '+projectId+'.')},
            success: function(data){
                if (data.length) {
                    datasets = data;
                    displaySavedDatasets();
                } else {
                    resetSavedDatasets();
                }
            }
        })
    }

    $(document).on('click','.view-dataset',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        XNAT.xhr.getJSON({
            url: rootUrl('/xapi/sets/collections/'+id),
            fail: function(e){ errorHandler(e,'Error trying to get the dataset '+id)},
            success: function(data){
                XNAT.plugin.collection.sets.viewDataset(data);
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
                XNAT.plugin.collection.sets.initDatasets();
            }
        })
    });

    /* --- init dashboard --- */

    sets.initDashboard = function(){
        definitions = []; // clear stored list of definitions
        getDatasetDefinitions();
        sets.initDatasets();
    };
    sets.initDatasets = function(){
        datasets = [];
        getSavedDatasets();
    };

    $(document).ready(function(){ sets.initDashboard(); })

}));