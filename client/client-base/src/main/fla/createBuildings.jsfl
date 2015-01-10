var lightYellow = 0xfcf3d4;
var yellow = 0xfdbc14;

var lightMagenta = 0xe8ddff;
var magenta = 0x9c6cff;

var lightCyan = 0xbffae9;
var cyan = 0x29d9dc;

var lightRed = 0xecb8bb;
var red = 0xfa6654;

var lightGrey = 0xeeeeee;
var grey = 0x999999;

var colors = [yellow, magenta, cyan, red, grey];
var lightColors = [lightYellow, lightMagenta, lightCyan, lightRed, lightGrey];

var sizes = [0.6, 0.8, 1];
var types = ["Buildings/Church", "Buildings/Tower", "Buildings/House"];

var x = 0;
var y = 0;
var i = 0;

// stroke - none
// font - white, helvetica bold, 20pt, center

var numbersLayer = fl.getDocumentDOM().getTimeline().layers[0].frames[0];
var buildingsLayer = fl.getDocumentDOM().getTimeline().layers[1].frames[0];
var shadowsLayer = fl.getDocumentDOM().getTimeline().layers[2].frames[0];
var groundsLayer = fl.getDocumentDOM().getTimeline().layers[3].frames[0];

for (var colorIndex = 0; colorIndex < colors.length; colorIndex++) {
    var color = colors[colorIndex];
    var lightColor = lightColors[colorIndex];

    for each(s in sizes) {
        for each(buildingType in types) {
            var fill = fl.getDocumentDOM().getCustomFill();
            fill.style = "solid";
            fill.color = 0xffffff;
            fl.getDocumentDOM().setCustomFill(fill);

            // number

            fl.getDocumentDOM().getTimeline().setSelectedLayers(0);
            fl.getDocumentDOM().addNewText({
                left: x,
                top: y,
                right: x + 28,
                bottom: y + 24
            }, Math.floor(Math.random() * 99).toString());
            var element = numbersLayer.elements[i];
            element.width *= s;
            element.height *= s;
            element.x = x - element.width / 2;
            element.y = y - 2 * s - element.height;

            // building
            fl.getDocumentDOM().getTimeline().setSelectedLayers(1);

            fl.getDocumentDOM().library.addItemToDocument({x: x, y: y}, buildingType);
            var element = buildingsLayer.elements[i];
            element.width *= s;
            element.height *= s;
            element.y = y;

            // shadow

            fl.getDocumentDOM().getTimeline().setSelectedLayers(2);
            fl.getDocumentDOM().library.addItemToDocument({x: x, y: y}, "Buildings/Shadow");

            var element = shadowsLayer.elements[i];
            element.width *= s;
            element.height *= s;

            // ground

            var fill = fl.getDocumentDOM().getCustomFill();
            fill.style = "solid";
            fill.color = lightColor;
            fl.getDocumentDOM().setCustomFill(fill);
            fl.getDocumentDOM().getTimeline().setSelectedLayers(3);
            fl.getDocumentDOM().addNewRectangle({
                left: x - 37 / 2,
                top: y - 37 + 4,
                right: x + 37 / 2,
                bottom: y + 4
            }, 0, false, false);

            x += 64;
            i++;
        }
    }
    x = 0;
    y += 100;
}