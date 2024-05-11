package com.example

import cats.effect.IOApp
import cats.effect.IO
import cats.syntax.all._
import org.http4s.client.Client

object Main extends IOApp.Simple {
  def run: IO[Unit] = IO.println("foo")
}
