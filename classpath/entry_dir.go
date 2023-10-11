package classpath

import (
	"os"
	"path/filepath"
)

type DirEntry struct {
	absDir	string
}

func newDirEntry(path string) *DirEntry {
	absDir, err := filepath.Abs(path)
	if err != nil {
		panic(err)
	}
	return &DirEntry{absDir}
}

func (self *DirEntry) readClass(className string) ([]byte, Entry, error) {
	fileName := filepath.Join(self.absDir, className)
	data, err := os.ReadFile(fileName)
	return data, self, err
}

func (slef *DirEntry) String() string {
	return slef.absDir
}
