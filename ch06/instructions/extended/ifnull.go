package extended

import (
	"github.com/tiny/jvm/ch06/instructions/base"
	"github.com/tiny/jvm/ch06/rtda"
)

// Branch if reference is null
type IFNULL struct{ base.BranchInstruction }

// Branch if reference not null
type IFNONNULL struct{ base.BranchInstruction }

func (self *IFNULL) Execute(frame *rtda.Frame) {
	ref := frame.OperandStack.PopRef()
	if ref == nil {
		base.Branch(frame, self.Offset)
	}
}
