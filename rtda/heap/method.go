package heap

type Method struct {
	ClassMember
	maxStack 		uint
	maxLocals 		uint
	code 			[]byte
}
