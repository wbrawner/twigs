package main

import (
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"os"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
	"github.com/urfave/cli/v2"
)

const ARG_PORT = "port"
const ARG_DB_NAME = "db-name"
const ARG_DB_USER = "db-user"
const ARG_DB_PASS = "db-pass"
const ARG_DB_HOST = "db-host"
const ARG_DB_PORT = "db-port"

func main() {
	app := &cli.App{
		Name:  "twigs",
		Usage: "personal/family finance app server",
		Flags: []cli.Flag{
			&cli.IntFlag{
				Name:    ARG_PORT,
				Value:   8080,
				Usage:   "the `PORT` for the server to listen on",
				Aliases: []string{"p"},
				EnvVars: []string{"TWIGS_PORT"},
			},
			&cli.StringFlag{
				Name:    ARG_DB_NAME,
				Value:   "twigs",
				Usage:   "the `NAME` of the database to connect to",
				EnvVars: []string{"TWIGS_DB_NAME"},
			},
			&cli.StringFlag{
				Name:    ARG_DB_USER,
				Value:   "twigs",
				Usage:   "the `USERNAME` to use to connect to the database",
				EnvVars: []string{"TWIGS_DB_USER"},
			},
			&cli.StringFlag{
				Name:    ARG_DB_PASS,
				Value:   "twigs",
				Usage:   "the `PASSWORD` to use to connect to the database",
				EnvVars: []string{"TWIGS_DB_PASS"},
			},
			&cli.StringFlag{
				Name:    ARG_DB_HOST,
				Value:   "twigs",
				Usage:   "the `HOST` to use to connect to the database",
				EnvVars: []string{"TWIGS_DB_HOST"},
			},
			&cli.IntFlag{
				Name:    ARG_DB_PORT,
				Value:   5432,
				Usage:   "the `PORT` to use to connect to the database",
				EnvVars: []string{"TWIGS_DB_PORT"},
			},
		},
		Action: func(c *cli.Context) error {
			_, err := sql.Open("postgres", fmt.Sprintf(
				"%s:%s@%s:%d/%s",
				c.String(ARG_DB_USER),
				c.String(ARG_DB_PASS),
				c.String(ARG_DB_HOST),
				c.Int(ARG_DB_PORT),
				c.String(ARG_DB_NAME),
			))
			if err != nil {
				log.Fatalf("Failed to connect to database %s on %s", c.String(ARG_DB_NAME), c.String(ARG_DB_HOST))
			}
			_ = mux.NewRouter()
			log.Fatal(http.ListenAndServe(":8080", nil))
			return nil
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
