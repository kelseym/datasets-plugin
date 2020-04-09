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

    XNAT.plugin.collection = collection =
        getObject(XNAT.plugin.collection || {});

    XNAT.plugin.collection.sets = sets =
        getObject(XNAT.plugin.collection.sets || {});

    XNAT.plugin.collection.sets.definitions = definitions =
        getObject(XNAT.plugin.collection.sets.definitions || {});

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

    function getSingleDefinition(id){
        return definitions.filter(function(d){ return d.id === id});
    }
    sets.editDefinition = function(id){
        var dfn = getSingleDefinition(id);
        XNAT.plugin.collection.sets.openDefinitionEditor(dfn);
    };
    sets.validateDefinition = function(id){
        var dfn = getSingleDefinition(id);
        XNAT.dialog.message('Placeholder Function','This will initiate the dataset validation process for '+dfn.label+'.');
    };
    sets.deleteDefinition = function(id){
        var dfn = getSingleDefinition(id);
        XNAT.dialog.message('Placeholder Function','This will delete the dataset definition named "'+dfn.label+'".');
    };

    $(document).on('click','.validate-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        sets.validateDefinition(id);
    });
    $(document).on('click','.edit-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        sets.editDefinition(id);
    });
    $(document).on('click','.delete-definition',function(e){
        e.preventDefault();
        var id = $(this).data('id');
        sets.deleteDefinition(id);
    });


    function displayDatasetDefinitions(container){
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
            .th({ style: { 'text-align': 'left' }},'Dataset Criteria')
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

    function resetDatasetDefinitions(container){
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

    sets.initDashboard = function(){
        definitions = []; // clear stored list of definitions
        getDatasetDefinitions();
    };

    $(document).ready(function(){ sets.initDashboard(); })

}));