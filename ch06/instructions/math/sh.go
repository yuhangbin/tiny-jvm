package math

import (
	"github.com/tiny/jvm/ch05/instructions/base"
	"github.com/tiny/jvm/ch05/rtda"
)

type ISHL struct { base.NoOperandsInstruction }

type IUSHR struct { base.NoOperandsInstruction }

type LSHL struct { base.NoOperandsInstruction }

type LSHR struct { base.NoOperandsInstruction }

type LUSHR struct { base.NoOperandsInstruction }

func (is *ISHL) Execute(frame *rtda.Frame)  {
	stack := frame.OperandStack
	v2 := stack.PopInt()
	v1 := stack.PopInt()
	s := uint32(v2) & 0x1f
	result := v1 << s
	stack.PushInt(result)
}

func (ls *LSHR) Execute(frame *rtda.Frame) {
	stack := frame.OperandStack
	v2 := stack.PopInt()
	v1 := stack.PopLong()
	s := uint32(v2) & 0x3f
	result := v1 >> s
	stack.PushLong(result)
}

func (self *IUSHR) Execute(frame *rtda.Frame)  {
	stack := frame.OperandStack
	v2 := stack.PopInt()
	v1 := stack.PopInt()
	s := uint32(v2) & 0x1f
	result := int32(uint32(v1) >> s)
	stack.PushInt(result)
}

