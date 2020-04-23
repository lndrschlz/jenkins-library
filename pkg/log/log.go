package log

import (
	"github.com/sirupsen/logrus"
<<<<<<< HEAD
	"strings"
=======

	"github.com/getsentry/sentry-go"
	"github.com/makasim/sentryhook"
>>>>>>> sentry hook added to logger
)

type RemoveSecretFormatterDecorator struct {
	logrus.TextFormatter
}

func (formatter *RemoveSecretFormatterDecorator) Format(entry *logrus.Entry) (bytes []byte, err error) {
	formattedMessage, err := formatter.TextFormatter.Format(entry)

	if err != nil {
		return nil, err
	}

	message := string(formattedMessage)

	for _, secret := range secrets {
		message = strings.Replace(message, secret, "****", -1)
	}

	return []byte(message), nil
}

// LibraryRepository that is passed into with -ldflags
var LibraryRepository string
var logger *logrus.Entry
var secrets []string

// Entry returns the logger entry or creates one if none is present.
func Entry() *logrus.Entry {
	if logger == nil {
		logger = logrus.WithField("library", LibraryRepository)
	}
<<<<<<< HEAD

	logger.Logger.SetFormatter(&RemoveSecretFormatterDecorator{})

=======
	if err := sentry.Init(sentry.ClientOptions{
		Dsn:              "https://4ebcc1b73e1e48348047faf82de0f05a:97557092cd4945789de7e1b28fa992b7@tools-sentry.mo.sap.corp/5",
		Environment:      "jenkins",
		AttachStacktrace: true,
	}); err != nil {
		logger.Fatal(err)
	}
	logger.Logger.AddHook(sentryhook.New([]logrus.Level{logrus.PanicLevel, logrus.FatalLevel, logrus.ErrorLevel}))
>>>>>>> sentry hook added to logger
	return logger
}

// SetVerbose sets the log level with respect to verbose flag.
func SetVerbose(verbose bool) {
	if verbose {
		//Logger().Debugf("logging set to level: %s", level)
		logrus.SetLevel(logrus.DebugLevel)
	}
}

// SetStepName sets the stepName field.
func SetStepName(stepName string) {
	logger = Entry().WithField("stepName", stepName)
}

// DeferExitHandler registers a logrus exit handler to allow cleanup activities.
func DeferExitHandler(handler func()) {
	logrus.DeferExitHandler(handler)
}

func RegisterSecret(secret string) {
	if len(secret) > 0 {
		secrets = append(secrets, secret)
	}
}
