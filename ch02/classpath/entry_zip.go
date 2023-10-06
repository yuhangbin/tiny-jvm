package classpath

import (
	"archive/zip"
	"errors"
	"io"
	"io/ioutil"
	"path/filepath"
)

type ZipEntry struct {
	absPath	string
}

func newZipEntry(path string) *ZipEntry {
	absPath, err := filepath.Abs(path)
	if err != nil {
		panic(err)
	}
	return &ZipEntry{absPath}
}

func (self *ZipEntry) readClass(className string) ([]byte, Entry, error) {
	r, err := zip.OpenReader(self.absPath)
	if err != nil {
		return nil, nil, err
	}
	defer r.Close()
	for _, file := range r.File {
		if file.Name == className {
			rc, err := file.Open()
			if err != nil {
				return nil, nil, err
			}
			defer rc.Close()
			data, err := io.ReadAll(rc)
			if err != nil {
				return nil, nil, err
			}
			return data, self, err
		}
	}
	return nil, nil, errors.New("class not found:" + className)
}

func (self *ZipEntry) String() string {
	return self.absPath
}
