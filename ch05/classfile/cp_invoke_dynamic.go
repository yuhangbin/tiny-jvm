package classfile

type ConstantInvokeDynamicInfo struct {
	BootstrapMethodAttrIndex	uint16
	NameAndTypeIndex 			uint16
}

func (self *ConstantInvokeDynamicInfo) readInfo(reader *ClassReader) {
	self.BootstrapMethodAttrIndex = reader.readUint16()
	self.NameAndTypeIndex = reader.readUint16()
}

type ConstantMethodHandleInfo struct {
	ReferenceKind	uint8
	ReferenceIndex	uint16
}

func (self *ConstantMethodHandleInfo) readInfo(reader *ClassReader) {
	self.ReferenceKind = reader.readUint8()
	self.ReferenceIndex = reader.readUint16()
}

type ConstantMethodTypeInfo struct {
	DescriptorIndex uint16
}

func (self *ConstantMethodTypeInfo) readInfo(reader *ClassReader) {
	self.DescriptorIndex = reader.readUint16()
}