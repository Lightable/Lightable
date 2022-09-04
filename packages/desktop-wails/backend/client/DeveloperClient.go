package client

import (
	"bufio"
	"bytes"
	"context"
	"fmt"
	"os"
	"os/exec"
	"strings"

	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type DeveloperClient struct {
	WorkDir string
	ctx *context.Context
}

func NewDeveloperClient(ctx *context.Context) *DeveloperClient {
	return &DeveloperClient{
		ctx: ctx,
	}
}

func (d *DeveloperClient) GetWorkDir() string {
	wd, err := os.Getwd()
	if err != nil {
		fmt.Printf("Could not get workdir: %v", err)
	}
	d.WorkDir = wd
	return wd
}

func (d *DeveloperClient) TestWailsInstall() (*string, error) {
	cmd := exec.Command("wails", "version")
	stdout, err := cmd.Output()
	if err != nil {
		return nil, err
	} else {
		str := string(stdout)
		fmt.Println(str)
		return &str, nil
	}
}

func (d *DeveloperClient) TestGCCInstall() (*string, error) {
	cmd := exec.Command("gcc", "--version")
	stderr := &bytes.Buffer{}
	stdout := new(strings.Builder)
	cmd.Stderr = stderr
	cmd.Stdout = stdout
	err := cmd.Run()
	if err != nil {
		return nil, err
	} else {
		str := stdout.String()
		return &str, nil
	}
}

func (d *DeveloperClient) ChangeWorkDir() string {
	dir, err := runtime.OpenDirectoryDialog(*d.ctx, runtime.OpenDialogOptions{
		DefaultDirectory: d.WorkDir,
		Title: "Set Development Work Directory",
	})
	if err != nil {
		fmt.Printf("Something went wrong when trying to fetch directory: %v\n", err)
		return ""
	}
	if (dir != "" && dir != " ") {
		d.WorkDir = dir
		return dir 
	}
	return ""
}


func (d *DeveloperClient) PNPMBuild() {
	  buildDir := d.WorkDir + "/frontend"
	  cmd := exec.Command("pnpm", "build")
	  cmd.Dir = buildDir
	  stdout, _ := cmd.StdoutPipe()
	  err := cmd.Start()
	  if err != nil {
		fmt.Printf("Got (error) output for build %v\n", err)
		return
	  }
	  
	  scanner := bufio.NewScanner(stdout)
	  scanner.Split(bufio.ScanLines)
	  for scanner.Scan() {
		  m := scanner.Text()
		  runtime.EventsEmit(*d.ctx, "dev:frontend|build", m)
	  }
	  // wait for command to finish
	  cmd.Wait()
}

func (d *DeveloperClient) WailsBuild() {
	buildDir := d.WorkDir
	cmd := exec.Command("wails", "build", "-nsis")
	cmd.Dir = buildDir
	stdout, _ := cmd.StdoutPipe()
	err := cmd.Start()
	if err != nil {
	  fmt.Printf("Got (error) output for build %v\n", err)
	  return
	}
	
	scanner := bufio.NewScanner(stdout)
	scanner.Split(bufio.ScanLines)
	for scanner.Scan() {
		m := scanner.Text()
		runtime.EventsEmit(*d.ctx, "dev:backend|build", m)
	}
	// wait for command to finish
	cmd.Wait()
}

