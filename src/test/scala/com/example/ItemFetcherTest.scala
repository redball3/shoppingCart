package com.example

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all._
import fs2.Stream
import munit.CatsEffectSuite
import org.http4s.EntityDecoder
import org.http4s.HttpApp
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.InvalidMessageBodyFailure

class ItemFetcherTest extends CatsEffectSuite {

  def clientWithResponse(body: String): Client[IO] = Client.apply[IO] { _ =>
    Resource.eval(IO(Response[IO](body = Stream.emits(body.getBytes))))
  }

  test("it should return shopping cart item if it can parse the response") {
    val expected = ShoppingCartItem("Cheerios", 8.43d)

    val goodResponse: String = """{"title": "Cheerios","price": 8.43}"""
    val client = clientWithResponse(goodResponse)

    val result = ItemFetcher.default[IO](client).getItem("doesntMatter")
    result.map(value => assertEquals(value, expected))
  }

  test("it should return an error if it cannot parse the response") {
    val expectedUri =
      s"https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/doesntMatter.json"
    val badResponse: String = """{"ohdear": "Cheerios","price": 8.43}"""

    val client = clientWithResponse(badResponse)

    val result = ItemFetcher.default[IO](client).getItem("doesntMatter").attempt
    result.map(value =>
      value.left.map(err => {
        err match {
          case InvalidMessageBodyFailure(details, cause) =>
            assert(details.contains("Could not decode JSON"))
          case _ =>
            fail("Client returned the wrong error for a bad response payload")

        }
      })
    )
  }
}
