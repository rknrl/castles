package ru.rknrl.castles.menu.screens.bank {
import ru.rknrl.dto.ProductDTO;

public class Products {
    private var products:Vector.<ProductDTO>;

    private var _product:ProductDTO;

    public function get product():ProductDTO {
        return _product;
    }

    public function Products(products:Vector.<ProductDTO>) {
        this.products = products;
        _product = getProductById(1);
    }

    public function getProductById(id:int):ProductDTO {
        for each(var product:ProductDTO in products) {
            if (product.id == id) return product;
        }
        throw new Error("can't find product " + id);
    }
}
}
