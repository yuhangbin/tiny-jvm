package constants

import (
	"github.com/tiny/jvm/ch04/rtda"
	"github.com/tiny/jvm/ch05/instructions/base"
)

type BIPUSH struct { val int8 } // push byte
type SIPUSH struct { val int16 } // push short

func (self *BIPUSH) FetchOperands(reader *base.BytecodeReader)  {
	self.val = reader.ReadInt8()
}

func (self *BIPUSH) Execute(frame *rtda.Frame)  {
	i := int32(self.val)
	frame.OperandStack.PushInt(i)
}