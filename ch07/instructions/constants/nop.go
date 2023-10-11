package constants

import (
	"github.com/tiny/jvm/ch06/instructions/base"
	"github.com/tiny/jvm/ch06/rtda"
)

type NOP struct{ base.NoOperandsInstruction }

func (self *NOP) Execute(frame *rtda.Frame) {
	// do nothing
}
