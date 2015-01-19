package ru.rknrl.castles.view.utils {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.easers.*;

public function interpolatePoint(prev:Point, next:Point, currentTime:int, startTime:int, duration:int, easer:IEaser):Point {
    const deltaTime:int = currentTime - startTime;
    var progress:Number = deltaTime / duration;
    if (progress >= 1) progress = 1;
    const value:Number = easer.ease(progress);
    return new Point(prev.x + (next.x - prev.x) * value, prev.y + (next.y - prev.y) * value);
}
}
