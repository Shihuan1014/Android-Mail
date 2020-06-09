/**
 * Copyright (C) 2017 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 var msize = 3,
   mcolor = '#050505',
   malign = 'left',
   mlist = false,
   mbold  = false,
   mblockQuote  =  false;

var RE = {}

var editor = document.getElementById('editor')
$("div").css("background","");
$("div").css("border-left","");
$("div").css("padding","15px 0px");
$("#editor").css("padding-bottom","100px");
RE.currentSelection = {
    "startContainer": editor,
    "startOffset": 0,
    "endContainer": editor,
    "endOffset": 0
};

RE.editor = document.getElementById('editor');

document.addEventListener("selectionchange", function() { RE.backuprange(); });

// Initializations
RE.callback = function() {
//    window.location.href = "re-callback://" + encodeURI(RE.getHtml());
window.location.href = "re-callback://hello";
}

RE.setHtml = function(contents) {
    RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
    setTimeout(function(){
        window.location.href = "re-callback://hello";
    },100)
}

RE.getHtml = function() {
    return RE.editor.innerHTML;
}

RE.getFinalHtml = function(){
    window.location.href = "re-final://" + encodeURI(RE.getHtml());
}

RE.getText = function() {
    return RE.editor.innerText;
}

RE.setBaseTextColor = function(color) {
    RE.editor.style.color  = color;
}

RE.setBaseFontSize = function(size) {
    RE.editor.style.fontSize = size;
}

RE.setPadding = function(left, top, right, bottom) {
  RE.editor.style.paddingLeft = left;
  RE.editor.style.paddingTop = top;
  RE.editor.style.paddingRight = right;
  RE.editor.style.paddingBottom = bottom;
}

RE.setBackgroundColor = function(color) {
    document.body.style.backgroundColor = color;
}

RE.setBackgroundImage = function(image) {
    RE.editor.style.backgroundImage = image;
}

RE.setWidth = function(size) {
    RE.editor.style.minWidth = size;
}

RE.setHeight = function(size) {
    RE.editor.style.height = size;
}

RE.setTextAlign = function(align) {
    RE.editor.style.textAlign = align;
}

RE.setVerticalAlign = function(align) {
    RE.editor.style.verticalAlign = align;
}

RE.setPlaceholder = function(placeholder) {
    RE.editor.setAttribute("placeholder", placeholder);
}

RE.setInputEnabled = function(inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.undo = function() {
    document.execCommand('undo', false, null);
}

RE.redo = function() {
    document.execCommand('redo', false, null);
}

RE.setBold = function() {
    document.execCommand('bold', false, null);
}

RE.setItalic = function() {
    document.execCommand('italic', false, null);
}

RE.setSubscript = function() {
    document.execCommand('subscript', false, null);
}

RE.setSuperscript = function() {
    document.execCommand('superscript', false, null);
}

RE.setStrikeThrough = function() {
    document.execCommand('strikeThrough', false, null);
}

RE.setUnderline = function() {
    document.execCommand('underline', false, null);
}

RE.setBullets = function() {
    document.execCommand('insertUnorderedList', false, null);
}

RE.setNumbers = function() {
    document.execCommand('insertOrderedList', false, null);
}

RE.setTextColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setTextBackgroundColor = function(color) {
    RE.restorerange();
    // styleWithCss 会开创一个节点 font ，而不是用text
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setFontSize = function(fontSize){
    document.execCommand("fontSize", false, fontSize);
}

RE.setHeading = function(heading) {
    document.execCommand('formatBlock', false, '<h'+heading+'>');
}

RE.setIndent = function() {
    document.execCommand('indent', false, null);
}

RE.setOutdent = function() {
    document.execCommand('outdent', false, null);
}

RE.setJustifyLeft = function() {
    document.execCommand('justifyLeft', false, null);
}

RE.setJustifyCenter = function() {
    document.execCommand('justifyCenter', false, null);
}

RE.setJustifyRight = function() {
    document.execCommand('justifyRight', false, null);
}

RE.setBlockquote = function() {
    try{
        if(mblockQuote){
            document.execCommand('formatBlock', false, '<div>');
            $("div").css("background","");
            $("div").css("border-left","");
            $("div").css("padding","8px 0px");
            $("div").css("color","#050505");
            $("#editor").css("padding-bottom","100px");
            mblockQuote = false
        }else{
            document.execCommand('formatBlock', false, '<blockquote>');
            $("blockquote").css("background","rgb(245, 246, 250)");
            $("blockquote").css("border-left","2px solid #D8D8D8");
            $("blockquote").css("margin","0 0 10px");
            $("blockquote").css("padding","14px 16px");
            $("blockquote").css("color","rgb(149, 149, 149)");
            mblockQuote = true
        }
    }catch(e){

    }
}

RE.insertImage = function(url, alt) {
    var html = '<img src="' + url + '" alt="' + alt + '" />';
    RE.insertHTML(html);
}

RE.insertHTML = function(html) {
    RE.restorerange();
    document.execCommand('insertHTML', false, html);
    RE.callback();
    setTimeout(function(){
        RE.callback();
    },500)
}

RE.insertLink = function(url, title) {
    RE.restorerange();
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML",false,"<a href='"+url+"'>"+title+"</a>");
    } else if (sel.rangeCount) {
       var el = document.createElement("a");
       el.setAttribute("href", url);
       el.setAttribute("title", title);

       var range = sel.getRangeAt(0).cloneRange();
       range.surroundContents(el);
       sel.removeAllRanges();
       sel.addRange(range);
   }
    RE.callback();
}

RE.setTodo = function(text) {
    var html = '<input type="checkbox" name="'+ text +'" value="'+ text +'"/> &nbsp;';
    document.execCommand('insertHTML', false, html);
}

RE.prepareInsert = function() {
    RE.backuprange();
}

RE.backuprange = function(){
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      RE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset};
    }
}

RE.restorerange = function(){
    try{
        var selection = window.getSelection();
        selection.removeAllRanges();
        var range = document.createRange();
        range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
        range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
        selection.addRange(range);
    }catch(e){

    }
}

RE.enabledEditingItems = function(e) {
    //我的部分，获得光标节点的Selection
    let l = window.getSelection().anchorNode;
    var list = []
    var checkFont = false;
    var checkColor = false;
//    递归检测
//    console.log(RE.getHtml())
    try{
        if(l!=null && l.parentNode.nodeName!='BODY' && l.parentNode.id != 'editor'){
//            console.log(l.textContent)
//            console.log(l.parentNode.nodeName)
//            console.log(l.parentNode.id)
            while(true){
                let tmp = l.parentNode
//                console.log(tmp)
                //检测到id，说明到根布局了
                if(tmp!=null && !tmp.hasAttribute('id')){
                    // 检查字体大小变化
                    if(tmp.hasAttribute('size')){
                        checkFont = true;
                        if(tmp.size!=msize){
                            list.push('size:'+tmp.size)
                            msize = tmp.size
                        }
                    }else if(tmp.hasAttribute('color')){
                        checkColor = true;
                        if(tmp.color!=mcolor){
                            list.push('color:'+tmp.color)
                            mcolor = tmp.color
                        }
                    }
                }else{
                    break;
                }
                l = tmp
            }
        }
        if(!checkFont){
            if(msize > 3){
//                console.log('没检测到size，但之前光标所在处的size不等于3')
                list.push('size:3')
                msize = 3
            }
        }
        //检查粗体
//        console.log("bold check: " +document.queryCommandState('bold'))
        if(document.queryCommandState('bold') && !mbold){
            list.push('bold:true')
            mbold = true
        }else if(!document.queryCommandState('bold') && mbold){
            list.push('bold:false')
            mbold = false
        }
        //检查引用
        var formatBlock = document.queryCommandValue('formatBlock');
        console.log(formatBlock.length + ' '+ mblockQuote)
        if (formatBlock.length == 10 && !mblockQuote) {
            list.push('blockquote:true')
            mblockQuote = true
        }else if(formatBlock.length < 10 && mblockQuote){
            list.push('blockquote:false')
            mblockQuote = false
        }
        //检查居中
        if (document.queryCommandState('justifyCenter') && malign != 'center') {
            malign = 'center'
            list.push('align:center')
        }else if(!document.queryCommandState('justifyCenter') && malign == 'center'){
            malign = 'left'
            list.push('align:left')
        }
        //没检测到color 但之前颜色不是原始色black
        if(!checkColor && mcolor != '#050505'){
            console.log('没检测到color: 原始color为：' + mcolor)
            mcolor = '#050505'
            list.push('color:#050505')
        }
    }catch(e){
    }
    if(list.length > 0){
          window.location.href = "re-state://" + encodeURI(list.join(',')) + '&&hello';

//      window.location.href = "re-state://" + encodeURI(list.join(',')) + '&&' + encodeURI(RE.getHtml());
    }
}

// 我的自定义，退格时，对于 bold 和 字体大小
RE.inputBackUpCheckState = function(e) {

}

RE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(RE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
}

RE.blurFocus = function() {
    RE.editor.blur();
}

RE.removeFormat = function() {
    document.execCommand('removeFormat', false, null);
}

// Event Listeners
RE.editor.addEventListener("input", function(e){
        if(document.queryCommandValue('formatBlock').length == 0){
            try{
                document.execCommand('formatBlock', false, '<div>');
                $("div").css("background","");
                $("div").css("border-left","");
                $("div").css("padding","8px 0px");
                $("#editor").css("padding-bottom","100px");
            }catch(e){

            }
        }
        RE.callback();
        RE.enabledEditingItems(e);
});
RE.editor.addEventListener("keyup", function(e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        RE.enabledEditingItems(e);
    }
    if(e.which == 13){
        if(document.queryCommandValue('formatBlock').length == 0){
        document.execCommand('formatBlock', false, '<div>');
        $("div").css("background","");
        $("div").css("border-left","");
        $("div").css("padding","15px 0px");
        $("#editor").css("padding-bottom","100px");
        }
    }
});
RE.editor.addEventListener("click", RE.enabledEditingItems);
