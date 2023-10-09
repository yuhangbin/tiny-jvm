package conversions

import (
	"github.com/tiny/jvm/ch05/instructions/base"
	"github.com/tiny/jvm/ch05/rtda"
)

type D2F struct { base.NoOperandsInstruction }
type D2I struct { base.NoOperandsInstruction }
type D2L struct { base.NoOperandsInstruction }

func (self *D2I) Execute(frame *rtda.Frame)  {
	stack := frame.OperandStack
	d := stack.PopDouble()
	i := int32(d)
	stack.PushInt(i)
}

