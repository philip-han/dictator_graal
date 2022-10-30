#include <stdbool.h>
#include "graal_isolate.h"

typedef struct CError {
    int code;
    char* message;
} CError;

typedef enum EitherType {
    intValue = 0,
    stringValue = 1
} EitherType;

typedef struct Either {
    CError* error;
    void* value;
    EitherType type;
} Either;

typedef void (*Start)(void);
typedef void (*End)(void);
typedef void (*Callback)(graal_isolatethread_t*, char*, float);
typedef void (*StateCallback)(graal_isolatethread_t*, bool);
typedef void (*RegisterCallback) (Callback);
typedef void (*RegisterStateCallback) (StateCallback);
typedef void (*Close)(void);

typedef struct SpeechRecognizer {
    Start start;
    End end;
    Close close;
    RegisterCallback registerCallback;
    RegisterStateCallback registerStateCallback;
} SpeechRecognizer;

typedef struct SpeechBusinessData {
    char* speech;
    char* confidenceRatingColor;
    char* sentiment;
    bool interrogative;
} SpeechBusinessData;

typedef void (*Dispatcher)(void*, SpeechBusinessData**);
typedef void (*StateDispatcher)(void*, bool);
typedef void (*ErrorDispatcher)(void*, char*);
