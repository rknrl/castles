package ru.rknrl.castles.view.utils {
public class Tweener {
    private static const epsilon:Number = 0.005;

    public function Tweener(speed:Number) {
        _speed = speed;
    }

    private var _speed:Number;

    public function set speed(value:Number):void {
        _speed = value;
    }

    public var nextValue:Number = 0;

    private var _value:Number = 0;

    public function set value(v:Number):void {
        _value = v;
    }

    public function get value():Number {
        return _value;
    }

    private var lastTime:int;

    public function update(time:int):void {
        const deltaTime:int = time - lastTime;
        lastTime = time;

        const delta:Number = nextValue - _value;

        if (Math.abs(delta) < epsilon) {
            _value = nextValue;
        } else {
            _value += delta * Math.min(1, deltaTime / _speed);
        }
    }
}
}
