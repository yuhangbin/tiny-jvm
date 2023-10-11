package heap

type Class struct {
	accessFlags 		uint16
	name 				string
	superClassName 		string
	interfaceNames 		[]string
	constantPool 		*ConstantPool
	fields 				[]*Field
	methods 			[]*Method
	loader 				*ClassLoader
	superClass 			*Class
	interfaces 			[]*Class
	instanceSlotCount 	uint
	staticSlotCount 	uint
	staticVars 			*Slots
}

