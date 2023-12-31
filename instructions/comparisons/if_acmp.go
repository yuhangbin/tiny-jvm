package comparisons

import (
	"github.com/tiny/jvm/ch07/instructions/base"
	"github.com/tiny/jvm/ch07/rtda"
)

// Branch if reference comparison succeeds
type IF_ACMPEQ struct{ base.BranchInstruction }
type IF_ACMPNE struct{ base.BranchInstruction }

func (self *IF_ACMPEQ) Execute(frame *rtda.Frame) {
	stack := frame.OperandStack
	ref2 := stack.PopRef()
	ref1 := stack.PopRef()
	if ref1 == ref2 {
		base.Branch(frame, self.Offset)
	}
}
