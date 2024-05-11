import munit.CatsEffectSuite
import cats.effect.IO
import com.example.ShoppingCart
import org.http4s.ember.client.EmberClient
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import com.example.ShoppingCartItem
import com.example.Totals
import com.example.ItemFetcher

class ShoppingCartTest extends CatsEffectSuite {
  val liveClient = EmberClientBuilder.default[IO].build

  test("you should be able to add an item to the shopping cart") {
    val itemToAdd = ShoppingCartItem("anItem", 123d)
    val expectedCart = ShoppingCart(List(itemToAdd))
    val cart =
      ShoppingCart(List.empty).addItem(itemToAdd)
    assertEquals(cart, expectedCart)
  }

  test("A shopping cart should total up to a known correct amount") {
    val expectedTotals =
      Totals(BigDecimal(15.02), BigDecimal(1.88d), BigDecimal(16.90d))
    val items = List(
      ShoppingCartItem("cornflakes", 2.52d),
      ShoppingCartItem("cornflakes", 2.52d),
      ShoppingCartItem("weetabix", 9.98d)
    )
    val cart = ShoppingCart(items)

    assertEquals(cart.totals, expectedTotals)
  }

  test("Tax calculation should be at 12.5%") {
    val expectedTotals =
      Totals(BigDecimal(100d), BigDecimal(12.5d), BigDecimal(112.5d))
    val cart = ShoppingCart(List(ShoppingCartItem("foo", 100d)))

    assertEquals(cart.totals, expectedTotals)
  }

  test("Tax calculation should be rounded up") {
    val expectedTotals =
      Totals(BigDecimal(103d), BigDecimal(12.88d), BigDecimal(115.88d))
    val cart = ShoppingCart(List(ShoppingCartItem("foo", 103d)))

    assertEquals(cart.totals, expectedTotals)
  }

  test("It should work with the http client") {
    val client =
      EmberClientBuilder.default[IO].withoutCheckEndpointAuthentication.build
    val cart = ShoppingCart(List.empty)
    val expectedCart = ShoppingCart(List(ShoppingCartItem("Cheerios", 8.43d)))

    val filledCart = client.use { cl =>
      val fetcher = ItemFetcher.default[IO](cl)
      for {
        cheerios <- fetcher.getItem("cheerios")
      } yield (cart.addItem(cheerios))
    }

    filledCart.map(crt => assertEquals(crt, expectedCart))
  }
}
