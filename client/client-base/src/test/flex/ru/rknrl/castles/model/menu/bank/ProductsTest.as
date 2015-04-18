//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.bank {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.ProductId;

public class ProductsTest {
    [Test("product")]
    public function t0():void {
        const product:ProductDTO = new ProductDTO();
        product.id = ProductId.STARS.id();

        const products:Products = new Products(new <ProductDTO>[product]);
        assertEquals(product, products.product)
    }

    [Test("can't find product", expects="Error")]
    public function t1():void {
        const product:ProductDTO = new ProductDTO();
        product.id = 0;

        new Products(new <ProductDTO>[product]).product;
    }
}
}
