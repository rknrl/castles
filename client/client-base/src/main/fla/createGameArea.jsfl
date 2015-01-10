var cell = 39;
var gap = 1;

// 15 x 15
// 8 x 11

var fill = fl.getDocumentDOM().getCustomFill();
fill.style = "solid";
fill.color = 0xc2ffa6;
fl.getDocumentDOM().setCustomFill(fill);

for (var i = 0; i < 15; i++) {
    for (var j = 0; j < 15; j++) {
        var x = i * cell + gap;
        var y = j * cell + gap;
        fl.getDocumentDOM().addNewRectangle({
            left: x,
            top: y,
            right: x + cell - gap * 2,
            bottom: y + cell - gap * 2
        }, 0)
    }
}