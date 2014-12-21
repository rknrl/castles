var cell = 39;
var gap = 1;

// 15 x 15
// 8 x 11

for (var i = 0; i < 8; i++) {
    for (var j = 0; j < 11; j++) {
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