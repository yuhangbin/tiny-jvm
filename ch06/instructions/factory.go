package instructions

import (
	"fmt"

	"github.com/tiny/jvm/ch06/instructions/base"
	"github.com/tiny/jvm/ch06/instructions/constants"
)

func NewInstruction(opcode byte) base.Instruction {
	switch opcode {
	case 0x00:
		return nop
	case 0x01:
		return aconst_null

	default:
		panic(fmt.Errorf("Unsupported opcode: 0x%x!", opcode))
	}
}

var (
	nop         = &constants.NOP{}
	aconst_null = &constants.ACONST_NULL{}
)
