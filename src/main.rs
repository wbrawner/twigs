use std::env;

use actix_web::{web, App, HttpResponse, HttpServer, Responder};

async fn pong() -> impl Responder {
    HttpResponse::Ok().body("PONG")
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let port: u16 = env::var("PORT").map_or(8080, |p| p.parse::<u16>().unwrap_or(8080));

    HttpServer::new(|| App::new().route("/ping", web::get().to(pong)))
        .bind(("0.0.0.0", port))?
        .run()
        .await
}
