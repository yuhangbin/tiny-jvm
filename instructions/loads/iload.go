package loads

import (
	"github.com/tiny/jvm/ch07/instructions/base"
	"github.com/tiny/jvm/ch07/rtda"
)

// load int from local variable
type ILOAD struct{ base.Index8Instruction }
type ILOAD_0 struct{ base.NoOperandsInstruction }
type ILOAD_1 struct{ base.NoOperandsInstruction }
type ILOAD_2 struct{ base.NoOperandsInstruction }
type ILOAD_3 struct{ base.NoOperandsInstruction }

func _iload(frame *rtda.Frame, index uint) {
	val := frame.LocalVars.GetInt(index)
	frame.OperandStack.PushInt(val)
}

func (self *ILOAD) FetchOperands(reader *base.BytecodeReader) {
	self.Index = uint(reader.ReadUint8())
}

func (self *ILOAD) Execute(frame *rtda.Frame) {
	_iload(frame, uint(self.Index))
}

func (self *ILOAD_1) Execute(frame *rtda.Frame) {
	_iload(frame, 1)
}
