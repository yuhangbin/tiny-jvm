package heap

import "github.com/tiny/jvm/ch06/classpath"

type ClassLoader struct {
	cp 			*classpath.Classpath
	classMap 	map[string]*Class // loaded classes
}
