package rtda

type Frame struct {
	lower 			*Frame
	LocalVars 		LocalVars
	OperandStack	*OperandStack
	Thread 			*Thread
	nextPC			int
}

func NewFrame(maxLocals, maxStack uint) *Frame {
	return &Frame{
		LocalVars: newLocalVars(maxLocals),
		OperandStack: newOperandStack(maxStack),
	}	
}

func (self *Frame) NextPC() int {
	return self.nextPC
}

func (self *Frame) SetNextPC(nextPC int)  {
	self.nextPC = nextPC
}

func Branch(frame *Frame, offset int)  {
	pc := frame.Thread.PC()
	nextPC := pc + offset
	frame.SetNextPC(nextPC)
}

func newFrame(thread *Thread, maxLocals, maxStack uint) *Frame {
	return &Frame{
		Thread: thread,
		LocalVars: newLocalVars(maxLocals),
		OperandStack: newOperandStack(maxStack),
	}
}