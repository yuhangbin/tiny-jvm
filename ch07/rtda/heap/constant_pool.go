package heap

type Constant interface {}
type ConstantPool struct {
	class 		*Class
	consts 		[]Constant
}
