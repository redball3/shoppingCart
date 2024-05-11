package com.example

import org.http4s.circe._
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class ShoppingCartItem(title: String, price: Double)
object ShoppingCartItem {
  implicit def encoder: Encoder[ShoppingCartItem] =
    deriveEncoder[ShoppingCartItem]
  implicit def decoder: Decoder[ShoppingCartItem] =
    deriveDecoder[ShoppingCartItem]
}

case class Totals(subTotal: BigDecimal, tax: BigDecimal, total: BigDecimal)

case class ShoppingCart(items: List[ShoppingCartItem]) {
  def addItem(itemToAdd: ShoppingCartItem): ShoppingCart =
    this.copy(items :+ itemToAdd)
  def totals: Totals = {
    val subTotal = items.map(item => BigDecimal(item.price)).sum
    val tax =
      (subTotal / 100 * 12.5d).setScale(2, BigDecimal.RoundingMode.HALF_UP)
    val total = subTotal + tax
    Totals(subTotal, tax, total)
  }
}
