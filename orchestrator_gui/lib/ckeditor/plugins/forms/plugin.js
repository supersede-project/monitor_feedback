﻿/*
 Copyright (c) 2003-2014, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.md or http://ckeditor.com/license
*/
CKEDITOR.plugins.add("forms",{requires:"dialog,fakeobjects",lang:"af,ar,bg,bn,bs,ca,cs,cy,da,de,el,en,en-au,en-ca,en-gb,eo,es,et,eu,fa,fi,fo,fr,fr-ca,gl,gu,he,hi,hr,hu,id,is,it,ja,ka,km,ko,ku,lt,lv,mk,mn,ms,nb,nl,no,pl,pt,pt-br,ro,ru,si,sk,sl,sq,sr,sr-latn,sv,th,tr,tt,ug,uk,vi,zh,zh-cn",icons:"button,checkbox,form,hiddenfield,imagebutton,radio,select,select-rtl,textarea,textarea-rtl,textfield",hidpi:!0,onLoad:function(){CKEDITOR.addCss(".cke_editable form{border: 1px dotted #FF0000;padding: 2px;}\n");
CKEDITOR.addCss("img.cke_hidden{background-image: url("+CKEDITOR.getUrl(this.path+"images/hiddenfield.gif")+");background-position: center center;background-repeat: no-repeat;border: 1px solid #a9a9a9;width: 16px !important;height: 16px !important;}")},init:function(b){var a=b.lang,e=0,h={email:1,password:1,search:1,tel:1,text:1,url:1},j={checkbox:"input[type,name,checked]",radio:"input[type,name,checked]",textfield:"input[type,name,value,size,maxlength]",textarea:"textarea[cols,rows,name]",select:"select[name,size,multiple]; option[value,selected]",
button:"input[type,name,value]",form:"form[action,name,id,enctype,target,method]",hiddenfield:"input[type,name,value]",imagebutton:"input[type,alt,src]{width,height,border,border-width,border-style,margin,float}"},k={checkbox:"input",radio:"input",textfield:"input",textarea:"textarea",select:"select",button:"input",form:"form",hiddenfield:"input",imagebutton:"input"},d=function(g,c,d){var h={allowedContent:j[c],requiredContent:k[c]};"form"==c&&(h.context="form");b.addCommand(c,new CKEDITOR.dialogCommand(c,
h));b.ui.addButton&&b.ui.addButton(g,{label:a.common[g.charAt(0).toLowerCase()+g.slice(1)],command:c,toolbar:"forms,"+(e+=10)});CKEDITOR.dialog.add(c,d)},f=this.path+"dialogs/";!b.blockless&&d("Form","form",f+"form.js");d("Checkbox","checkbox",f+"checkbox.js");d("Radio","radio",f+"radio.js");d("TextField","textfield",f+"textfield.js");d("Textarea","textarea",f+"textarea.js");d("Select","select",f+"select.js");d("Button","button",f+"button.js");var i=b.plugins.image;i&&!b.plugins.image2&&d("ImageButton",
"imagebutton",CKEDITOR.plugins.getPath("image")+"dialogs/image.js");d("HiddenField","hiddenfield",f+"hiddenfield.js");b.addMenuItems&&(d={checkbox:{label:a.forms.checkboxAndRadio.checkboxTitle,command:"checkbox",group:"checkbox"},radio:{label:a.forms.checkboxAndRadio.radioTitle,command:"radio",group:"radio"},textfield:{label:a.forms.textfield.title,command:"textfield",group:"textfield"},hiddenfield:{label:a.forms.hidden.title,command:"hiddenfield",group:"hiddenfield"},button:{label:a.forms.button.title,
command:"button",group:"button"},select:{label:a.forms.select.title,command:"select",group:"select"},textarea:{label:a.forms.textarea.title,command:"textarea",group:"textarea"}},i&&(d.imagebutton={label:a.image.titleButton,command:"imagebutton",group:"imagebutton"}),!b.blockless&&(d.form={label:a.forms.form.menu,command:"form",group:"form"}),b.addMenuItems(d));b.contextMenu&&(!b.blockless&&b.contextMenu.addListener(function(g,c,b){if((g=b.contains("form",1))&&!g.isReadOnly())return{form:CKEDITOR.TRISTATE_OFF}}),
b.contextMenu.addListener(function(b){if(b&&!b.isReadOnly()){var c=b.getName();if(c=="select")return{select:CKEDITOR.TRISTATE_OFF};if(c=="textarea")return{textarea:CKEDITOR.TRISTATE_OFF};if(c=="input"){var a=b.getAttribute("type")||"text";switch(a){case "button":case "submit":case "reset":return{button:CKEDITOR.TRISTATE_OFF};case "checkbox":return{checkbox:CKEDITOR.TRISTATE_OFF};case "radio":return{radio:CKEDITOR.TRISTATE_OFF};case "image":return i?{imagebutton:CKEDITOR.TRISTATE_OFF}:null}if(h[a])return{textfield:CKEDITOR.TRISTATE_OFF}}if(c==
"img"&&b.data("cke-real-element-type")=="hiddenfield")return{hiddenfield:CKEDITOR.TRISTATE_OFF}}}));b.on("doubleclick",function(a){var c=a.data.element;if(!b.blockless&&c.is("form"))a.data.dialog="form";else if(c.is("select"))a.data.dialog="select";else if(c.is("textarea"))a.data.dialog="textarea";else if(c.is("img")&&c.data("cke-real-element-type")=="hiddenfield")a.data.dialog="hiddenfield";else if(c.is("input")){c=c.getAttribute("type")||"text";switch(c){case "button":case "submit":case "reset":a.data.dialog=
"button";break;case "checkbox":a.data.dialog="checkbox";break;case "radio":a.data.dialog="radio";break;case "image":a.data.dialog="imagebutton"}if(h[c])a.data.dialog="textfield"}})},afterInit:function(b){var a=b.dataProcessor,e=a&&a.htmlFilter,a=a&&a.dataFilter;CKEDITOR.env.ie&&e&&e.addRules({elements:{input:function(a){var a=a.attributes,b=a.type;b||(a.type="text");("checkbox"==b||"radio"==b)&&"on"==a.value&&delete a.value}}},{applyToAll:!0});a&&a.addRules({elements:{input:function(a){if("hidden"==
a.attributes.type)return b.createFakeParserElement(a,"cke_hidden","hiddenfield")}}},{applyToAll:!0})}});
CKEDITOR.env.ie&&(CKEDITOR.dom.element.prototype.hasAttribute=CKEDITOR.tools.override(CKEDITOR.dom.element.prototype.hasAttribute,function(b){return function(a){this.$.attributes.getNamedItem(a);if("input"==this.getName())switch(a){case "class":return 0<this.$.className.length;case "checked":return!!this.$.checked;case "value":var e=this.getAttribute("type");return"checkbox"==e||"radio"==e?"on"!=this.$.value:!!this.$.value}return b.apply(this,arguments)}}));