package base

import "github.com/tiny/jvm/ch06/rtda"

func Branch(frame *rtda.Frame, offset int) {
	pc := frame.Thread.PC()
	nextPC := pc + offset
	frame.SetNextPC(nextPC)
}
