package stores

import (
	"github.com/tiny/jvm/ch06/instructions/base"
	"github.com/tiny/jvm/ch06/rtda"
)

// store long into local variable
type LSTORE struct{ base.Index8Instruction }
type LSTORE_0 struct{ base.NoOperandsInstruction }
type LSTORE_1 struct{ base.NoOperandsInstruction }
type LSTORE_2 struct{ base.NoOperandsInstruction }
type LSTORE_3 struct{ base.NoOperandsInstruction }

func _lstore(frame *rtda.Frame, index uint) {
	val := frame.OperandStack.PopLong()
	frame.LocalVars.SetLong(index, val)
}

func (ls *LSTORE) Execute(frame *rtda.Frame) {
	_lstore(frame, uint(ls.Index))
}

func (ls *LSTORE_2) Execute(frame *rtda.Frame) {
	_lstore(frame, 2)
}
