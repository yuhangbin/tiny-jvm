package constants

import (
	"github.com/tiny/jvm/ch04/rtda"
	"github.com/tiny/jvm/ch05/instructions/base"
)

type NOP struct { base.NoOperandsInstruction }

func (self *NOP) Execute(frame *rtda.Frame)  {
	// do nothing
}

