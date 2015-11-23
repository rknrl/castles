//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.top {
import org.flexunit.asserts.assertEquals;

import protos.Top;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.userInfo.CastlesUserInfo;

public class TopUtilsTest {
    [Test("getPlace")]
    public function t0():void {
        const top:Top = DtoMock.top();
        const user1:CastlesUserInfo = TopUtils.getPlace(top, 1);
        const user2:CastlesUserInfo = TopUtils.getPlace(top, 2);
        assertEquals(1, top.weekNumber);
        assertEquals(DtoMock.topUser1.info.firstName, user1.firstName);
        assertEquals(DtoMock.topUser2.info.firstName, user2.firstName);
    }

    [Test("getPlace invalid")]
    public function t1():void {
        const top:Top = DtoMock.top();
        assertEquals(1, top.weekNumber);
        assertEquals("Somebody", TopUtils.getPlace(top, 0).firstName);
    }
}
}
