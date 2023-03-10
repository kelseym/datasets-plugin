/*
 * ml-plugin: datasetDashboard.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
    var plugin, collection, sets, definitions;

    XNAT.plugin = plugin =
        getObject(XNAT.plugin || {});

    XNAT.plugin.datasets = collection =
        getObject(XNAT.plugin.datasets || {});

    XNAT.plugin.datasets.sets = sets =
        getObject(XNAT.plugin.datasets.sets || {});

    XNAT.plugin.datasets.sets.definitions = definitions =
        getObject(XNAT.plugin.datasets.sets.definitions || []);

    var urlParams = new URLSearchParams(window.location.search);
    var projectId = urlParams.get('project') || urlParams.get('id');
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
        XNAT.dialog.confirm({
            title: 'Delete Definition?',
            content: 'Are you sure you want to delete this dataset definition? This operation cannot be undone.',
            okAction: function(){
                XNAT.xhr.ajax({
                    method: 'DELETE',
                    url: csrfUrl('/xapi/sets/definitions/projects/'+projectId+'/'+id),
                    fail: function(e){ errorHandler(e, 'Error attempting to delete dataset with id '+id)},
                    success: function(){
                        XNAT.ui.banner.top(2000, 'Definition deleted.', 'success');
                        sets.initDashboard();
                    }
                })
            }
        });
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
        XNAT.plugin.datasets.sets.openDefinitionEditor(dfn);
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
        container.empty().append(spawn('div.message','No dataset criteria have been defined in this '+XNAT.app.displayNames.singular.project.toLowerCase()+'. Currently supported dataset types are: "TaggedResourceMap".'))
    }

    function evaluateDefinitionResponse(data){
        if (data.length) {
            definitions = data;
            displayDatasetDefinitions();
        } else {
            resetDatasetDefinitions();
        }
    }

    function getDatasetDefinitions(){
        XNAT.xhr.getJSON({
            url: rootUrl('/xapi/sets/definitions/projects/'+projectId),
            fail: function(e){
                // add a workaround for false-positive jQuery XHR errors
                if (/ok/i.test(e.statusText)) {
                    var data = e.responseText;
                    evaluateDefinitionResponse(data);
                }
                errorHandler(e,'Error trying to retrieve dataset definitions for '+projectId+'.')},
            success: function(data){
                evaluateDefinitionResponse(data)
            }
        })
    }

    /* --- handle dataset validations --- */

    function getValidationUrl(id){
        // Temporary validation URL
        // var swaggerhub = 'https://virtserver.swaggerhub.com/hortonw/detailed_dataset_report/1.0.0';
        // return swaggerhub + '/sets/definitions/detailed_report/'+projectId+'/'+id;

        return csrfUrl('/xapi/sets/definitions/report/'+projectId);
    }
    function evaluateResult(experiment,list){
        var valid = true;
        experiment.results.forEach(function(result){ if (!result.scans.length) { valid = false }});
        if (valid) { list.push(experiment.id) }
        return list;
    }
    
    function displayValidationResults(items){
        // sort list of items
        items = items.sort(function(a,b){ return (a.label > b.label) ? 1 : -1 });

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
            return spawn('a',{ href: rootUrl('/data/experiments/'+experiment.id+'?format=html'), style: { 'font-weight':'bold' }}, experiment.label)
        }
        function displayIcon(validation){
            return (validation.scans.length) ?
                spawn('span.view-matching-scans',{ title: 'Matching Scan ID(s): '+validation.scans.join(', '), data: { scans: JSON.stringify(validation.scans) }}, validation.scans.length + ' scans found') :
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
            spawn('div.message','The Tagged Resource Map expects to find one resource for each tagged scan caategory, i.e. "image" and "label". Depending on the construction of your dataset definition and the structure of your data, 0, 1, or more matches may be found, which may not produce the dataset structure you desire.'),
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
    $(document).on('click','.view-matching-scans',function(){
        // var matchingScans = JSON.parse($(this).data('scans'));
        var matchingScans = $(this).data('scans');
        XNAT.ui.dialog.message({
            title: 'Matching Scans Detail',
            content: '<p>Note: All resource files matched in these scans will be included in the dataset.</p><p>Scan IDs: '+matchingScans.join(', ')+'</p>'
        });
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
        var projectLabel = XNAT.app.displayNames.singular.project.toLowerCase();
        projectLabel = projectLabel[0].toUpperCase() + projectLabel.slice(1,projectLabel.length);
        xmodal.loading.open('Validating '+projectLabel+' Data');
        XNAT.xhr.postJSON({
            url: getValidationUrl(),
            data: JSON.stringify(dfn.criteria[0].payload),
            processData: false,
            dataType: 'text',
            fail: function(e){
                xmodal.loading.close();
                errorHandler(e,'Error trying to query the dataset validation URL for dataset definition '+id);
                disablePanel();
            },
            success: function(data){
                xmodal.loading.close();
                var results = JSON.parse(data);
                if (results.length) {
                    displayValidationResults(results,dfn);
                    enablePanel(id);
                } else {
                    XNAT.dialog.message('Error','Could not validate your project data against this data definition. No results were returned.');
                }
            }
        });
    };

    sets.saveDataset = function(id){
        xmodal.loading.open('Creating Dataset...');
        XNAT.xhr.ajax({
            method: 'POST',
            url: csrfUrl('/xapi/sets/definitions/'+id),
            fail: function(e){
                xmodal.loading.close();
                errorHandler(e,'Error trying to save dataset from definition '+id);
            },
            success: function(data){
                xmodal.loading.close();
                XNAT.ui.banner.top(2000,'New dataset '+data.label+' created with '+data.fileCount+' files.','success');
                XNAT.plugin.datasets.sets.initDatasets();
            }
        })
    };

    /* --- init dashboard --- */

    sets.initDashboard = function(){
        definitions = []; // clear stored list of definitions
        getDatasetDefinitions();
        sets.initDatasets();
    };
    sets.initDatasets = function(){
        XNAT.plugin.datasets.projDatasets.getSavedDatasets();
    };

    $(document).ready(function(){ sets.initDashboard(); })

}));