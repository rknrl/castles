//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.top {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.userInfo.TopUserInfo;

public class TopTest {
    [Test("getPlace")]
    public function t0():void {
        const top:Top = new Top(DtoMock.top());
        const user1:TopUserInfo = top.getPlace(1);
        const user2:TopUserInfo = top.getPlace(2);
        assertEquals(1, top.weekNumber);
        assertEquals(1, user1.place);
        assertEquals(DtoMock.topUser1.info.firstName, user1.info.firstName);
        assertEquals(2, user2.place);
        assertEquals(DtoMock.topUser2.info.firstName, user2.info.firstName);
    }

    [Test("getPlace invalid")]
    public function t1():void {
        const top:Top = new Top(DtoMock.top());
        assertEquals(1, top.weekNumber);
        assertEquals(0, top.getPlace(0).place);
    }
}
}
