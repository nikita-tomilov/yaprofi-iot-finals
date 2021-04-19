#g++ -L/usr/lib main.cc -I/usr/include -o main -lrrd
ifeq ($(wildcard ./Makefile.inc), )
	$(error Configuration not found. Run ./configure first)
endif

include ./Makefile.inc

BINARY_PREFIX = rf24

SRC_DIR := ./src
INC_DIR += ./inc
OBJ_DIR := ./obj
SRC_FILES := $(wildcard $(SRC_DIR)/*.cpp)
OBJ_FILES := $(patsubst $(SRC_DIR)/%.cpp,$(OBJ_DIR)/%.o,$(SRC_FILES))

EXEC := cppudp

LIBS=-l$(LIB)
ifeq ($(DRIVER), LittleWire)
	LIBS+= -llittlewire-spi
endif
LIBS += -lrf24network

all: $(OBJ_FILES)
	$(CXX) $(LDFLAGS) -L$(LIB_DIR) $^ $(LIBS) -o $(EXEC)

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp objdir
	$(CXX) $(CFLAGS) -I$(HEADER_DIR)/.. -I$(INC_DIR) -L$(LIB_DIR) -c $< $(LIBS) -o $@

objdir:
	mkdir -p $(OBJ_DIR)

clean:
	rm -rf $(OBJ_DIR) $(EXEC)
