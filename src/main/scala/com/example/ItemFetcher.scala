package com.example

import org.http4s.client.Client
import org.http4s.Uri
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import io.circe.parser._
import scala.util.control.NoStackTrace
import org.http4s.Status
import cats.Monad
import cats.syntax.all._
import cats.effect.Async

trait ItemFetcher[F[_]] {
  def getItem(itemName: String): F[ShoppingCartItem]
}

final case class UnexpectedResponseError(
    status: Status,
    body: String,
    uri: String
) extends RuntimeException
    with NoStackTrace {
  override def getMessage: String =
    s"unexpected response $status for request at $uri. body: $body"
}

object ItemFetcher {
  def default[F[_]](
      client: Client[F]
  )(implicit F: Async[F]): ItemFetcher[F] = {
    new ItemFetcher[F] {
      def getItem(itemName: String): F[ShoppingCartItem] = {
        val uri =
          s"https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/${itemName.toLowerCase}.json"
        implicit val entitydecoder: EntityDecoder[F, ShoppingCartItem] =
          jsonOf[F, ShoppingCartItem]

        client.expectOr[ShoppingCartItem](uri)(resp => {
          resp.body
            .through(fs2.text.utf8.decode)
            .take(4096)
            .compile
            .string
            .flatMap { body =>
              F.pure(UnexpectedResponseError(resp.status, body, uri))
            }
        })
      }
    }
  }
}
