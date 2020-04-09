console.log('datasetDefinition.js');

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
}(function(){

    var undef;
    var plugin, collection, sets;

    XNAT.plugin = plugin =
        getObject(XNAT.plugin || {});

    XNAT.plugin.collection = collection =
        getObject(XNAT.plugin.collection || {});

    XNAT.plugin.collection.sets = sets =
        getObject(XNAT.plugin.collection.sets || {});

    var projectId = XNAT.data.context.project;
    var rootUrl   = XNAT.url.rootUrl;
    var restUrl   = XNAT.url.restUrl;
    var csrfUrl   = XNAT.url.csrfUrl;

    function probablyJSON(it){
        return /string/i.test(typeof it) && /^[{[]/.test((it).trim());
    }

    function possiblyJSON(it){
        return it && probablyJSON(it) ? JSON.parse(it) : it;
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

    function definitionEditor(dfn){

        // var readonly = (opts && opts.readonly) ? opts.readonly : false;
        // var cfgLabel = opts.label;

        dfn = getObject(dfn);

        var id      = dfn.id || '';
        var criteria = (dfn.criteria) ? JSON.stringify(dfn.criteria[0].payload) : '{}';

        var _source = spawn('textarea', criteria);
        var _editor = XNAT.app.codeEditor.init(_source, {
            language: 'json'
        });

        sets.criteria = _editor;
        var labelInput = (dfn.label) ?
            spawn('input.def-label|name=label|size=30|value='+dfn.label) :
            spawn('input.def-label|name=label|size=30');

        function dfnMeta(){
            return spawn('form.definition-metadata', { style: { marginBottom: 0 } }, [
                id && ['input|type=hidden|name=id', { value: id }],
                ['input|type=hidden|name=project', { value: dfn.project || projectId }],
                ['table.xnat-table.clean.compact.borderless|style=width:100%', [
                    ['tbody', [
                        ['tr', [
                            ['td|style=width:200px', '<b>Label</b> &nbsp;'],
                            ['td', [ labelInput ]]
                        ]],
                        ['tr', [
                            ['td.top', '<b>Description</b> &nbsp;'],
                            ['td', [
                                ['textarea.def-description|type=text|name=description', {
                                    html: dfn.description || '',
                                    style: { width: '100%' },
                                    attr: { rows: 4 }
                                }]
                            ]]
                        ]],
                        ['tr', [
                            ['td.top', '<b>Resolver</b> &nbsp;'],
                            ['td', [
                                ['select.def-resolver|name=resolver', [
                                    ['option', {
                                        value: 'TaggedResourceMap',
                                        selected: 'selected'
                                    }, 'Tagged Resource Map']
                                ]]
                            ]]
                        ]],
                        ['tr', [
                            ['td', '<b>Criteria (File Matchers Only)</b> &nbsp;'],
                            ['td']
                        ]]
                    ]]
                ]],
                ''
            ])
        }


        // handle either pre-serialized JSON string or JSON object/array
        function processCriteria(criteria){
            return JSON.stringify(possiblyJSON(criteria));
        }


        _editor.openEditor({
            title: dfn.id ? 'Edit Dataset Definition <b>' + dfn.label + '</b>' : 'New Dataset Definition',
            width: 800,
            height: 450,
            classes: 'plugin-json',
            footerContent: 'Save Dataset Definition?',
            before: dfnMeta(),
            // source: criteria,
            // beforeShow: function(dialog){
            //     dialog.body$.prepend(dfnMeta());
            // },
            // afterShow: function(dialog, obj){
            //     dialog.setHeight();
            //     // obj.aceEditor.setReadOnly(readonly);
            // },
            buttons: {
                save: {
                    label: 'Save',
                    isDefault: true,
                    close: false,
                    action: function(dialog){

                        var defData = {
                            project: projectId,
                            label: dialog.body$.find('[name="label"]').val(),
                            description: dialog.body$.find('[name="description"]').val(),
                            criteria: [
                                {
                                    resolver: dialog.body$.find('[name="resolver"]').find('option:selected').val(),
                                    payload: processCriteria(_editor.aceEditor.getValue())
                                }
                            ]

                        };

                        console.log(defData);

                        // return;

                        if (!dfn.id) {
                            XNAT.xhr.postJSON({
                                url: rootUrl('/xapi/sets/definitions'),
                                data: JSON.stringify(defData),
                                success: function(){
                                    dialog.close();
                                    XNAT.ui.banner.top(2000, 'Definition saved.', 'success');
                                    XNAT.plugin.collection.sets.initDashboard();
                                }
                            })
                        } else {
                            XNAT.xhr.putJSON({
                                url: csrfUrl('/xapi/sets/definitions/projects/'+dfn.project+'/'+dfn.id),
                                data: JSON.stringify(defData),
                                success: function(){
                                    dialog.close();
                                    XNAT.ui.banner.top(2000, 'Definition updated.', 'success');
                                    XNAT.plugin.collection.sets.initDashboard();
                                }
                            })
                        }


                    }
                },
                close: {
                    label: 'Cancel',
                    close: true
                }
            }
        });

    }
    sets.openDefinitionEditor = function(definition){
        definitionEditor(definition);
    };

    sets.createDefinition = function(){
        definitionEditor({});
    };

}));
