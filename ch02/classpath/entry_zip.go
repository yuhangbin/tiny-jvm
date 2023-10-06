package classpath

type ZipEntry struct {
	absDir	string
}

func newZipEntry(path string) *ZipEntry {
	
}

func (self *ZipEntry) readClass(className string) ([]byte, Entry, error) {
	
}

func (self *ZipEntry) String() string {
	return self.absDir
}
