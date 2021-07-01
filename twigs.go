package main

import (
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"os"

	"github.com/gorilla/mux"
	_ "github.com/go-sql-driver/mysql"
)

func getEnvOrDefault(env string, defaultVal string) string {
	envVal := os.Getenv(env)
	if envVal == "" {
		return defaultVal
	} else {
		return envVal
	}
}

func main() {
	dbName := getEnvOrDefault("TWIGS_DB_NAME", "budget")
	dbUser := getEnvOrDefault("TWIGS_DB_USER", "budget")
	dbPass := getEnvOrDefault("TWIGS_DB_PASS", "budget")
	dbHost := getEnvOrDefault("TWIGS_DB_HOST", "localhost")
	dbPort := getEnvOrDefault("TWIGS_DB_PORT", "3306")
	db, err := sql.Open("mysql", fmt.Sprintf("%s:%s@%s:%s/%s", dbUser, dbPass, dbHost, dbPort, dbName))
	if err != nil {
		log.Fatalf("Failed to connect to database %s on %s", dbName, dbHost)
	}
	router := mux.NewRouter()
	log.Fatal(http.ListenAndServe(":8080", nil))
}