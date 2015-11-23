//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.bank {
import org.flexunit.asserts.assertEquals;

import protos.Product;
import protos.ProductId;

public class ProductsTest {
    [Test("product")]
    public function t0():void {
        const product:Product = new Product();
        product.id = ProductId.STARS.id();

        const products:Products = new Products(new <Product>[product]);
        assertEquals(product, products.product)
    }

    [Test("can't find product", expects="Error")]
    public function t1():void {
        const product:Product = new Product();
        product.id = 0;

        new Products(new <Product>[product]).product;
    }
}
}
