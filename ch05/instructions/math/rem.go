package math

import (
	"github.com/tiny/jvm/ch05/rtda"
	"github.com/tiny/jvm/ch05/instructions/base"
)

type DREM struct { base.NoOperandsInstruction }
type FREM struct { base.NoOperandsInstruction }
type IREM struct { base.NoOperandsInstruction }
type LREM struct { base.NoOperandsInstruction }


func (self *IREM) Execute(frame *rtda.Frame)  {
	
}