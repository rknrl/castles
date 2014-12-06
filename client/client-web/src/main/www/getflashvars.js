flashVars = {};
var href = document.location.href;
var tmp = href.substr(href.indexOf('?') + 1).split('&');
var i = tmp.length;
while (i--) {
    var v = tmp[i].split('=');
    flashVars[v[0]] = decodeURIComponent(v[1]);
}
