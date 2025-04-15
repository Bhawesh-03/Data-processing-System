package main

import (
	"fmt"
	"log"
	"os"
	"sync"
	"time"
)

var (
	numWorkers = 4
	tasks      = make(chan string, 10)
	wg         sync.WaitGroup
	logger     *log.Logger
)

func main() {
	setupLogger()
	loadTasks()

	for i := 0; i < numWorkers; i++ {
		wg.Add(1)
		go worker(i)
	}

	wg.Wait()
	logger.Println("All workers completed.")
}

func setupLogger() {
	logFile, err := os.OpenFile("log.txt", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
	if err != nil {
		fmt.Println("Could not set up logger:", err)
		os.Exit(1)
	}
	logger = log.New(logFile, "LOG: ", log.Ldate|log.Ltime|log.Lshortfile)
}

func loadTasks() {
	for i := 1; i <= 10; i++ {
		tasks <- fmt.Sprintf("Task-%d", i)
	}
	close(tasks)
}

func worker(id int) {
	defer wg.Done()
	logger.Printf("Worker %d started.\n", id)
	for task := range tasks {
		processTask(id, task)
		err := saveResult(id, task)
		if err != nil {
			logger.Printf("Worker %d error writing result: %v\n", id, err)
		}
	}
	logger.Printf("Worker %d finished.\n", id)
}

func processTask(id int, task string) {
	time.Sleep(1 * time.Second) // simulate processing
	logger.Printf("Worker %d processed %s\n", id, task)
}

func saveResult(id int, result string) error {
	file, err := os.OpenFile("results.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = file.WriteString(fmt.Sprintf("Processed by worker %d: %s\n", id, result))
	return err
}
